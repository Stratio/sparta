/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
declare module '*.json' {
    const value: any;
    export default value;
}

declare module '*.svg' {
    const content: string;
    export default content;
}

declare module '*';

// Type definitions for tween.js v16.9.0
// Project: https://github.com/tweenjs/tween.js/
// Definitions by: jordan <https://github.com/Amos47>, sunetos <https://github.com/sunetos>, jzarnikov <https://github.com/jzarnikov>, alexburner <https://github.com/alexburner>
// Definitions: https://github.com/DefinitelyTyped/DefinitelyTyped

interface Easing {
    Linear: {
        None(k: number): number;
    };
    Quadratic: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Cubic: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Quartic: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Quintic: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Sinusoidal: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Exponential: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Circular: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Elastic: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Back: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
    Bounce: {
        In(k: number): number;
        Out(k: number): number;
        InOut(k: number): number;
    };
}

interface Interpolation {
    Linear(v: number[], k: number): number;
    Bezier(v: number[], k: number): number;
    CatmullRom(v: number[], k: number): number;

    Utils: {
        Linear(p0: number, p1: number, t: number): number;
        Bernstein(n: number, i: number): number;
        Factorial(n: number): number;
        CatmullRom(p0: number, p1: number, p2: number, p3: number, t: number): number;
    };
}
