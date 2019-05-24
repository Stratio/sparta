/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export function getEdgePosition(svgAuxDef: any, x1: number, y1: number, x2: number, y2: number, h: number, w: number) {

  const diff = Math.abs(x1 - x2);
  if (diff > w + 16) {
    y1 += h / 2;
    y2 += h / 2;
    if (x1 > x2) {
      x2 += w;
    } else {
      x1 += w;
    }
    const d1 = x1 - x2;
    const d2 = y1 - y2;
    const xt = x2 + (x1 - x2) / 2;
    const diffX = Math.abs(d1);
    const diffY = Math.abs(d2);
    if (diffX < 300 && diffY < 300) {
      const min = diffX < diffY ? diffX : diffY;
      const rad = min < 28 ? Math.floor(min / 2) : 14;

      if (d2 < 0) {
        // top-right
        if (d1 > 0) {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${xt + rad},${y1} ` +
            `A ${rad},${rad} 0 0 0 ${xt}, ${y1 + rad}` +
            `L${xt},${y2 - rad}` +
            `A ${rad},${rad} 0 0 1 ${xt - rad},${y2} ` +
            `L${x2},${y2}`);
          // top-left
        } else {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${xt - rad},${y1} ` +
            `A ${rad},${rad} 0 0 1 ${xt}, ${y1 + rad}` +
            `L${xt},${y2 - rad}` +
            `A ${rad},${rad} 0 0 0 ${xt + rad},${y2} ` +
            `L${x2},${y2}`);
        }
      } else {
        // bottom-right
        if (d1 > 0) {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${xt + rad},${y1}` +
            `A ${rad},${rad} 0 0 1 ${xt}, ${y1 - rad}` +
            `L${xt},${y2 + rad}` +
            `A ${rad},${rad} 0 0 0 ${xt - rad},${y2} ` +
            `L${x2},${y2}`);
          // bottom-left
        } else {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${xt - rad},${y1}` +
            `A ${rad},${rad} 0 0 0 ${xt}, ${y1 - rad}` +
            `L${xt},${y2 + rad}` +
            `A ${rad},${rad} 0 0 1 ${xt + rad},${y2} ` +
            `L${x2},${y2}`);
        }
      }
    } else {
      svgAuxDef.attr('d', 'M' + x1 + ',' + y1 + ' C' + xt + ',' + y1 + ' ' + xt + ',' + y2 + ' ' + x2 + ',' + y2);
    }

  } else {
    x1 += w / 2;
    x2 += w / 2;
    if (y1 > y2) {
      y2 += h;
    } else {
      y1 += h;
    }
    const yt = y2 + (y1 - y2) / 2;
    const d1 = x1 - x2;
    const d2 = y1 - y2;
    const xt = x2 + (x1 - x2) / 2;
    const diffX = Math.abs(d1);
    const diffY = Math.abs(d2);
    if (diffX < 300 && diffY < 300) {
      const min = diffX < diffY ? diffX : diffY;
      const rad = min < 28 ? Math.floor(min / 2) : 14;

      if (d2 < 0) {
        // top-right
        if (d1 > 0) {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${x1},${yt - rad} ` +
            `A ${rad},${rad} 0 0 1 ${x1 - rad}, ${yt}` +
            `L${x2 + rad},${yt}` +
            `A ${rad},${rad} 0 0 0 ${x2},${yt + rad} ` +
            `L${x2},${y2}`);
          // top-left
        } else {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${x1},${yt - rad} ` +
            `A ${rad},${rad} 0 0 0 ${x1 + rad}, ${yt}` +
            `L${x2 - rad},${yt}` +
            `A ${rad},${rad} 0 0 1 ${x2},${yt + rad} ` +
            `L${x2},${y2}`);
        }
      } else {
        // bottom-right
        if (d1 > 0) {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${x1},${yt + rad}` +
            `A ${rad},${rad} 0 0 0 ${x1 - rad}, ${yt}` +
            `L${x2 + rad},${yt}` +
            `A ${rad},${rad} 0 0 1 ${x2},${yt - rad} ` +
            `L${x2},${y2}`);
          // bottom-left
        } else {
          svgAuxDef.attr('d',
            `M${x1},${y1} L${x1},${yt + rad}` +
            `A ${rad},${rad} 0 0 1 ${x1 + rad}, ${yt}` +
            `L${x2 - rad},${yt}` +
            `A ${rad},${rad} 0 0 0 ${x2},${yt - rad} ` +
            `L${x2},${y2}`);
        }
      }
    } else {
      svgAuxDef.attr('d', 'M' + x1 + ',' + y1 + ' C' + x1 + ',' + yt + ' ' + x2 + ',' + yt + ' ' + x2 + ',' + y2);
    }
  }
  return {
    x1, y1, x2, y2
  };
}

export const getBezierEdge = function(svgAuxDef: any, x1: number, y1: number, x2: number, y2: number, h: number, w: number) {
  if (!svgAuxDef) {
    return;
  }
  const diff = Math.abs(x1 - x2);
  if (diff > w + 16) {
    y1 += h / 2;
    y2 += h / 2;
    if (x1 > x2) {
      x2 += w;
    } else {
      x1 += w;
    }
    svgAuxDef.attr('d', 'M' + x1 + ',' + y1 + ' C' + x2 + ',' + y1 + ' ' + x1 + ',' + y2 + ' ' + x2 + ',' + y2);
  } else {
    x1 += w / 2;
    x2 += w / 2;
    if (y1 > y2) {
      y2 += h;
    } else {
      y1 += h;
    }
    svgAuxDef.attr('d', 'M' + x1 + ',' + y1 + ' C' + x1 + ',' + y2 + ' ' + x2 + ',' + y1 + ' ' + x2 + ',' + y2);
  }
  return {
    x1, y1, x2, y2
  };
};
