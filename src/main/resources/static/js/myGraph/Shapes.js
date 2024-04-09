/**
 * Copyright (c) 2006-2015, JGraph Ltd
 */

/**
 * Registers shapes.
 */
(function () {
    mxEdgeStyle.WireConnector = function (state, source, target, hints, result) {
        // Creates array of all way- and terminalpoints
        var pts = state.absolutePoints;
        var horizontal = state.cell.position;
        if (state.cell.position != null) {
            source.cell.position = state.cell.position;
        }
        if (horizontal == null) {
            horizontal = source.cell.position;
        }
        var hint = null;


        // Adds the first point
        // TODO: Should move along connected segment
        var pt = pts[0];
        pt = pt.clone();
        var first = pt;

        // Adds the waypoints
        if (hints != null && hints.length > 0) {

            for (var i = 0; i < hints.length; i++) {
                horizontal = !horizontal;
                hint = state.view.transformControlPoint(state, hints[i]);

                if (horizontal) {
                    if (pt.y != hint.y) {
                        pt.y = hint.y;
                        result.push(pt.clone());
                    }
                } else if (pt.x != hint.x) {
                    pt.x = hint.x;
                    result.push(pt.clone());
                }
            }
        } else {
            hint = pt;
        }

        // Adds the last point
        pt = pts[pts.length - 1];

        // TODO: Should move along connected segment
        if (pt == null && target != null) {
            pt = new mxPoint(state.view.getRoutingCenterX(target), state.view.getRoutingCenterY(target));
        }

        if (horizontal) {
            if (pt.y != hint.y && first.x != pt.x) {
                result.push(new mxPoint(pt.x, hint.y));
            }
        } else if (pt.x != hint.x && first.y != pt.y) {
            result.push(new mxPoint(hint.x, pt.y));
        }
    };

    mxStyleRegistry.putValue('wireEdgeStyle', mxEdgeStyle.WireConnector);

    var graphCreateEdgeHandler = Graph.prototype.createEdgeHandler;
    Graph.prototype.createEdgeHandler = function (state, edgeStyle) {
        if (edgeStyle == mxEdgeStyle.WireConnector) {
            var handler = new mxEdgeSegmentHandler(state);
            return handler;
        }
        return graphCreateEdgeHandler.apply(this, arguments);
    };


    mxRectangleShape.prototype.constraints = [
        new mxConnectionConstraint(new mxPoint(0.5, 0), true,null, null, null,"up"),//上边
        new mxConnectionConstraint(new mxPoint(0, 0.5), true,null, null, null,"left"),//左边
          new mxConnectionConstraint(new mxPoint(1, 0.5), true,null, null, null,"right"),//右边
           new mxConnectionConstraint(new mxPoint(0.5, 1), true,null, null, null,"down"),//下边

    ];

})();