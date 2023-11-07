# MlogWatcher 

the Mindustry mod for Logic JavaScript Compiler watch mode.

## Principle
This mod watches for changes to a specific `.mlog` file (the custom file extension which contains mlog).   
When a new compilation is made, the file being watched changes, and the mode detects this and injects the file contents into the selected logic processor.

## Config

Because of the principle described above, you must first select the path to the file to be watched before using it.

![image](https://github.com/Sharlottes/MlogWatcher/assets/60801210/7a061cde-f4be-476f-909d-8de18c87f3b2)

## How to use it

just select the processor. that's all!   
The processor injector compresses and injects code remotely into the selected processor, so you don't need to enter the canvas or anything like that.

![java_jcWgh8Mx2d](https://github.com/Sharlottes/MlogWatcher/assets/60801210/023853dd-ae84-491a-8460-acfbac18716a)

if you click the block which isn't the processor, the processor will be unselected.

## Demo

https://github.com/Sharlottes/MlogWatcher/assets/60801210/09305ce7-0775-45a1-9072-dd4728e5d442

## MlogJSWatchTemplate

If you are using [mlogjs](https://mlogjs.github.io/mlogjs), I recommend using [mlogJSTemplate](https://github.com/Sharlottes/mlogJsWatchTemplate) as the development environment.   
This template repository is also ready for watch mode: if you edit and save `script.ts`, it will automatically compile.   
so if you use it with this mod, you have a complete continuous development flow!
