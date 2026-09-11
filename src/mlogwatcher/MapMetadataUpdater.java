package mlogwatcher;

import arc.Core;
import arc.Events;
import arc.audio.Music;
import arc.audio.Sound;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.*;
import arc.util.io.PropertiesUtils;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.ctype.ContentType;
import mindustry.editor.MapInfoDialog;
import mindustry.editor.MapLocalesDialog;
import mindustry.editor.MapObjectivesDialog;
import mindustry.editor.MapProcessorsDialog;
import mindustry.editor.data.MapAssetsDialog;
import mindustry.game.EventType;
import mindustry.game.MapObjectives;
import mindustry.graphics.Pal;
import mindustry.io.JsonIO;
import mindustry.logic.LVar;
import mindustry.mod.DataPatcher;
import mindustry.mod.data.*;
import mindustry.type.MapLocales;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import mlogwatcher.websocket.api.LogicProcessor;
import mlogwatcher.websocket.api.ProcessorUpdateResults;
import mlogwatcher.websocket.api.ProgramId;
import mlogwatcher.websocket.api.Response;

import java.awt.*;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static mindustry.Vars.*;

public class MapMetadataUpdater {
    /**
     * @param path relative path
     * @param text json or hjson string
     * @see mindustry.editor.data.MapPatchesView#addPatch(String, int) @MapPatchesView#addPatch
     */
    public static void updatePatch(Path path, String text) {
        Seq<PatchAsset> patches = state.data.getPatches();
        Seq<PatchAsset> oldPatches = patches.copy();
        String fileName = Strings.getFileNameWithoutExtension(path.toString());
        PatchAsset asset = new PatchAsset(text);

        asset.setPath("patch-" + fileName.replace('\\', '_') + "-" + UUID.randomUUID() + ".json");

        try {
            Jval.read(text); //validation

            int replaceIndex = patches.indexOf(p ->  p.patch.split("\n")[0].replace(" ", "").equalsIgnoreCase(text.split("\n")[0].replace(" ", "")));

            if(replaceIndex == -1) {
                patches.add(asset);
            } else {
                patches.set(replaceIndex, asset);
            }

            state.data.reloadPatches(patches);

            Log.info("[MlogWatcher] successfully updated patch " + fileName);
        } catch(Exception e) {
            patches.set(oldPatches);
        }
    }

    /**
     * @param path should be /{items,blocks,...,unitStances}/**\/*.{json,hjson}
     * @param text json or hjson string
     * @see mindustry.ctype.ContentType
     * @see mindustry.editor.data.MapContentView#addContent(MapAssetsDialog, ContentType, String, String)@MapContentView#addContent
     */
    public static void updateContent(Path path, String text) {
        String typeDirectory = path.subpath(1, 2).toString();
        @Nullable ContentType contentType = Structs.find(ContentType.all, type -> type.folderName.equalsIgnoreCase(typeDirectory));
        Seq<ContentAsset> contents = state.data.getContent();
        ContentAsset asset = new ContentAsset(path.getFileName().toString(), contentType, text);
        int replaceIndex = contents.indexOf(p ->  p.path.equalsIgnoreCase(asset.path) || p.name.equalsIgnoreCase(asset.name));

        if(contentType == null) {
            Log.warn("[MlogWatcher] unknown content type: " + typeDirectory);
            return;
        } else if(replaceIndex == -1) {
            contents.add(asset);
        } else {
            contents.set(replaceIndex, asset);
        }

        state.data.reloadContent(false);
        state.data.regenerateContentSprites(false);

        Log.info("[MlogWatcher] successfully updated content "+ path);
    }

    /**
     * @param path relative path
     * @param text .properties text
     * @see mindustry.editor.data.MapBundlesView#buildButtons(MapAssetsDialog, Table)@MapBundlesView#buildButtons
     */
    public static void updateTranslation(Path path, String text) {
        try {
            PropertiesUtils.load(new StringMap(), new StringReader(text));

            byte[] bytes = text.getBytes(Strings.utf8);
            Seq<BundleAsset> bundles = state.data.getBundles();
            int replaceIndex = bundles.indexOf(p -> p.path.equalsIgnoreCase(path.toString()));
            BundleAsset bundle = new BundleAsset();

            bundle.setPath(path.toString());
            bundle.updateData(bytes);

            if(replaceIndex == -1) {
                bundles.add(bundle);
            } else {
                bundles.set(replaceIndex, bundle);
            }

            Log.info("[MlogWatcher] successfully updated translation "+ path);
        } catch(Exception e) {
            Log.warn("[MlogWatcher] Unexpected exception on updating : " +  e);
        }
    }

    /**
     * @param file image file
     * @see mindustry.editor.data.MapImagesView#buildButtons(MapAssetsDialog, Table)@MapImagesView#buildButtons
     */
    public static void updateImageFile(Fi file) {
        try {
            Seq<ImageAsset> images = state.data.getImages();
            Pixmap pix = new Pixmap(file);
            int width = pix.width;
            int height = pix.height;
            Pixmaps.bleed(pix);
            byte[] bytes = PixmapIO.writePngBytes(pix);
            pix.dispose();

            if(width > DataPatcher.maxImageSize || height > DataPatcher.maxImageSize) {
                Log.warn(Core.bundle.format("asset.image.toolarge", width, height, DataPatcher.maxImageSize, DataPatcher.maxImageSize));

                return;
            }

            String name = file.nameWithoutExtension();
            String path = file.name();
            int replaceIndex = images.indexOf(p -> p.path.equalsIgnoreCase(path) || p.name.equalsIgnoreCase(name));
            byte[] hash = assetCache.add(bytes);
            ImageAsset asset = new ImageAsset(path, hash);

            if(replaceIndex == -1) {
                images.add(asset);
            } else {
                images.set(replaceIndex, asset);
            }

            images.sort();
            state.data.regenerateContentSprites(true);

            Log.info("[MlogWatcher] successfully updated image " + file.name());
        } catch(Exception e) {            
            Log.warn("[MlogWatcher] Unexpected exception on updating : " +  e);
        }
    }

    /**
     * @param file audio file
     * @see mindustry.editor.data.MapAudioView#buildButtons(MapAssetsDialog, Table)@MapAudioView#buildButtons
     */
    public static void updateAudioFile(DataAssetType type, Fi file) {
        try {
            Seq<DataAsset> assets = state.data.getAssets(type);
            String name = file.nameWithoutExtension();
            String path = file.name();
            int replaceIndex = assets.indexOf(p -> p.path.equalsIgnoreCase(path) || p.name.equalsIgnoreCase(name));
            DataAsset asset = (type == DataAssetType.music ? new MusicAsset() : new SoundAsset());

            asset.setPath(path);
            asset.updateData(file.readBytes());

            if(replaceIndex == -1) {
                assets.add(asset);
            } else {
                assets.set(replaceIndex, asset);
            }

            assets.sort();
            state.data.reloadAudio();

            Log.info("[MlogWatcher] successfully updated audio " + file.name());
        } catch(Exception e) {
            Log.warn("[MlogWatcher] Unexpected exception on updating : " +  e);
        }
    }

    /**
     * @param text formatted map locale bundle text
     * @see mindustry.editor.MapLocalesDialog
     */
    public static void updateBundle(String text) {
        MapLocalesDialog localesDialog = Reflect.get((MapInfoDialog) Reflect.get(Vars.ui.editor, "infoDialog"), "locales");
        MapLocales locales = Reflect.invoke(localesDialog, "readBundles", new Object[]{ text }, String.class);

        Reflect.set(localesDialog, "locales", locales);
        Reflect.set(localesDialog, "lastSaved", locales.copy());
        editor.tags.put("locales", JsonIO.write(locales));
        state.mapLocales = locales;

        Log.info("[MlogWatcher] successfully updated locales");
    }

    /**
     * @param text formatted map locale bundle text
     * @see mindustry.editor.MapObjectivesDialog
     */
    public static void updateObjective(String text) {
        try{
            MapObjectives objectives = JsonIO.read(MapObjectives.class, text);

            if(objectives != null) {
                state.rules.objectives = objectives;
            }
        }catch(Exception e) {
            Log.err(e);
        }

        Log.info("[MlogWatcher] successfully updated objectives");
    }
}
