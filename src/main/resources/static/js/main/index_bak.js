// 程序启动方法
function main(container, status, category) {
    // 浏览器兼容检测
    if (!mxClient.isBrowserSupported()) {
        mxUtils.error('Browser is not supported!', 200, false);
    } else {
        // 显示导航线
        mxGraphHandler.prototype.guidesEnabled = true;
        // IE浏览器样式修复
        if (mxClient.IS_QUIRKS) {
            document.body.style.overflow = 'hidden';
            new mxDivResizer(container);
            new mxDivResizer(status);
        }
        // 禁用浏览器默认的右键菜单栏
        mxEvent.disableContextMenu(container);
        // 禁用ALT键
        mxGuide.prototype.isEnabledForEvent = function (evt) {
            return !mxEvent.isAltDown(evt);
        };
        // 启用终点捕捉
        mxEdgeHandler.prototype.snapToTerminals = true;

        // 创建图形编辑器
        var editor = new mxEditor();
        var graph = editor.graph;
        //设置图形容器，并配置编辑器
        editor.setGraphContainer(container);
        // 滚轮缩放
        // mxEvent.addMouseWheelListener(function (evt, up) {
        //     if (up) {
        //         graph.zoomIn();
        //     } else {
        //         graph.zoomOut();
        //     }
        //     mxEvent.consume(evt);
        // });
        // 不可改变大小
        graph.setCellsResizable(false);
        // 可否重复连接
        graph.setMultigraph(true);
        // 是否允许Cells通过其中部的连接点新建连接,false则通过连接线连接
        graph.setConnectable(true);
        // 设置元素可编辑
        // graph.setCellsLocked(false);
        //是否允许线单独存在
        graph.setAllowDanglingEdges(false);
        // 是否允许连线的目标和源是同一元素
        graph.setAllowLoops(false);
        // 不可修改
        graph.setCellsEditable(false);
        // 开启可编辑
        graph.setEnabled(true);
        // 节点是否解析html
        graph.setHtmlLabels(false);
        // 配置样式
        configureStylesheet(graph);
        // 键盘支持
        new mxKeyHandler(graph);

        // 设置默认组
        // var group = new mxCell('Group', new mxGeometry(), 'group');
        // group.setVertex(true);
        // group.setConnectable(false);
        // editor.defaultGroup = group;
        // editor.groupBorderSize = 12;

        // 键盘删除选中Cell或者Edge
        var keyHandler = new mxKeyHandler(graph);
        keyHandler.bindKey(46, function (evt) {
            if (graph.isEnabled()) {
                graph.removeCells();
            }
        });

        // 在窗口底部添加按钮
        status.innerHTML = "";
        for (let i = 0; i < bottomButtons.length; i++) {
            var btn = bottomButtons[i];
            addToolbarButton(editor, status, btn.action, btn.label,
                btn.img, btn.isTransparent, btn.title);
        }

        // 保存图形
        editor.addAction('save', function () {
            var enc = new mxCodec(mxUtils.createXmlDocument());
            var node = enc.encode(graph.getModel());
            console.log(mxUtils)
            var value = mxUtils.getXml(node);

            saveGraph(value, category); // 保存图形
        });

        // 重写group, ungroup
        editor.addAction('groupOrUngroup', function (editor, cell) {
            cell = cell || graph.getSelectionCell();
            if (cell != null && graph.isSwimlane(cell)) {
                editor.execute('ungroup', cell);
            } else {
                editor.execute('group');
            }
        });

        // 重写label
        graph.getLabel = function (cell) {
            if (cell.value) {
                return cell.value.split(",")[0];
            }
            return cell.value;
        };

        // 定义锚点----------start
        mxGraph.prototype.getAllConnectionConstraints = function (terminal, source) {
            if (terminal != null && terminal.shape != null) {
                if (terminal.shape.stencil != null) {
                    return terminal.shape.stencil.constraints;
                } else if (terminal.shape.constraints != null) {
                    return terminal.shape.constraints;
                }
            }

            return null;
        };
        mxShape.prototype.constraints = [
            new mxConnectionConstraint(new mxPoint(0.2, 0), true),
            new mxConnectionConstraint(new mxPoint(0.4, 0), true),
            new mxConnectionConstraint(new mxPoint(0.6, 0), true),
            new mxConnectionConstraint(new mxPoint(0.8, 0), true),
            new mxConnectionConstraint(new mxPoint(0, 0.2), true),
            new mxConnectionConstraint(new mxPoint(0, 0.4), true),
            new mxConnectionConstraint(new mxPoint(0, 0.6), true),
            new mxConnectionConstraint(new mxPoint(0, 0.8), true),
            new mxConnectionConstraint(new mxPoint(1, 0.2), true),
            new mxConnectionConstraint(new mxPoint(1, 0.4), true),
            new mxConnectionConstraint(new mxPoint(1, 0.6), true),
            new mxConnectionConstraint(new mxPoint(1, 0.8), true),
            new mxConnectionConstraint(new mxPoint(0.2, 1), true),
            new mxConnectionConstraint(new mxPoint(0.4, 1), true),
            new mxConnectionConstraint(new mxPoint(0.6, 1), true),
            new mxConnectionConstraint(new mxPoint(0.8, 1), true)];
        mxPolyline.prototype.constraints = null;
        // 定义锚点----------end

        // 图形回显
        viewGraph(graph, editor, category);

        // 组织结构树
        initOrgTree(graph, editor);

        // TAB切换
        $(".layui-tab-title li").on('click', function () {
            var category = $(this).attr("id");
            viewGraph(graph, editor, category);
        })
    }
};

function initOrgTree(graph, editor) {
    var setting = {
        data: {
            simpleData: {
                enable: true
            }
        },
        check: {
            enable: true,
            chkboxType: {"Y": "", "N": ""}
        },
        callback: {
            onClick: function (event, treeId, treeNode) {
                var category = $(".layui-this").attr("id");
                viewGraph(graph, editor, category, treeNode.id);
            }
        }
    };
    $.get($("#orgTree").data("url"), function (result) {
        var zNodes = [];
        result.data.forEach(function (item) {
            var org = {
                id: item.id,
                pId: item.pid,
                name: item.title
            };
            if (item.pid === "0" || item.pid === "1") {
                org.open = true;
                org.checked = true;
            }
            zNodes.push(org);
        });
        $.fn.zTree.init($("#orgTree"), setting, zNodes);
    });
}

// 保存图形
function saveGraph(value, category) {
    var orgId;
    var zTree = $.fn.zTree.getZTreeObj("orgTree");
    let nodes = zTree.getSelectedNodes();
    if (nodes.length > 0) {
        orgId = nodes[0].id;
    }

    var v = {"xml": value, "category": category, "orgId": orgId};
    $.ajax({
        url: "/system/graph/save",
        type: "post",
        data: JSON.stringify(v),
        contentType: 'application/json;charset=utf-8',
        success: function (result) {
            if (result.msg) {
                layer.alert(result.msg);
            } else {
                layer.alert("权限不足");
            }
        }
    });
}

// 图形回显
function viewGraph(graph, editor, category, orgId) {
    // 清空画布
    graph.selectAll();
    editor.execute('delete');

    var v = {"orgId": orgId, "category": category};
    $.ajax({
        url: "/system/graph/index",
        type: "post",
        data: JSON.stringify(v),
        contentType: 'application/json;charset=utf-8',
        success: function (result) {
            if (result.data) {
                let model = graph.getModel();
                model.beginUpdate();

                try {
                    var xmlDoc = mxUtils.parseXml(result.data);
                    var codec = new mxCodec(xmlDoc);
                    codec.decode(xmlDoc.documentElement, model);
                } finally {
                    model.endUpdate();
                }
            }
        }
    });
}

// 配置样式
function configureStylesheet(graph) {
    // 顶点样式
    var style = new Object();
    style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_IMAGE;
    style[mxConstants.STYLE_IMAGE_WIDTH] = '48PX';
    style[mxConstants.STYLE_IMAGE_HEIGHT] = '48PX';
    style[mxConstants.STYLE_VERTICAL_LABEL_POSITION] = mxConstants.ALIGN_BOTTOM; // label在正下方
    style[mxConstants.STYLE_LABEL_BACKGROUNDCOLOR] = '#FFFFFF';
    style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
    style[mxConstants.STYLE_SPACING_BOTTOM] = '50px'
    graph.getStylesheet().putDefaultVertexStyle(style);

    // 连线样式
    style = graph.getStylesheet().getDefaultEdgeStyle();
    style[mxConstants.STYLE_STROKECOLOR] = '#00FF00';
    style[mxConstants.STYLE_LABEL_BACKGROUNDCOLOR] = '#FFFFFF'; // 连线文字背景颜色
    graph.getStylesheet().getDefaultEdgeStyle()['edgeStyle'] = 'orthogonalEdgeStyle'; // 折线
    delete graph.getStylesheet().getDefaultEdgeStyle()["endArrow"]; //去掉箭头
    graph.getStylesheet().putDefaultEdgeStyle(style);

    // 分组样式
    style = new Object();
    style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_SWIMLANE;
    style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
    style[mxConstants.STYLE_ALIGN] = mxConstants.ALIGN_CENTER;
    style[mxConstants.STYLE_VERTICAL_ALIGN] = mxConstants.ALIGN_TOP;
    style[mxConstants.STYLE_FILLCOLOR] = '#FF9103';
    style[mxConstants.STYLE_GRADIENTCOLOR] = '#F8C48B';
    style[mxConstants.STYLE_STROKECOLOR] = '#E86A00';
    style[mxConstants.STYLE_FONTCOLOR] = '#000000';
    style[mxConstants.STYLE_ROUNDED] = true;
    style[mxConstants.STYLE_OPACITY] = '80';
    style[mxConstants.STYLE_STARTSIZE] = '30';
    style[mxConstants.STYLE_FONTSIZE] = '16';
    style[mxConstants.STYLE_FONTSTYLE] = 1;
    graph.getStylesheet().putCellStyle('group', style);
};

// 添加底部工具栏按钮
function addToolbarButton(editor, toolbar, action, label, image, isTransparent, title) {
    var button = document.createElement('button');
    button.style.fontSize = '10';
    if (image != null) {
        var img = document.createElement('img');
        img.setAttribute('src', image);
        img.style.width = '16px';
        img.style.height = '16px';
        img.style.verticalAlign = 'middle';
        img.style.marginRight = '2px';
        button.appendChild(img);
        button.style.marginRight = '12px';
        button.style.cursor = 'pointer';
        button.title = title;
    }
    if (isTransparent) {
        button.style.background = 'transparent';
        button.style.color = '#FFFFFF';
        button.style.border = 'none';
    }
    mxEvent.addListener(button, 'click', function (evt) {
        editor.execute(action);
    });
    mxUtils.write(button, label);
    toolbar.appendChild(button);
};

// 底部按钮
var bottomButtons = [
    // 组合/分解
    // {
    //     'title': '组合/分解',
    //     'action': 'groupOrUngroup',
    //     'label': '',
    //     'img': '/graph/images/icon/group.png',
    //     'isTransparent': true
    // },
    // 删除
    {
        'title': '删除',
        'action': 'delete',
        'label': '',
        'img': '/graph/images/icon/delete.png',
        'isTransparent': true
    },
    // 剪切
    // {
    //     'title': '剪切',
    //     'action': 'cut',
    //     'label': '',
    //     'img': '/graph/images/icon/cut.png',
    //     'isTransparent': true
    // },
    // 复制
    // {
    //     'title': '按住Ctrl拖动复制',
    //     'action': 'copy',
    //     'label': '',
    //     'img': '/graph/images/icon/copy.png',
    //     'isTransparent': true
    // },
    // 粘贴
    // {
    //     'title': '粘贴',
    //     'action': 'paste',
    //     'label': '',
    //     'img': '/graph/images/icon/paste.png',
    //     'isTransparent': true
    // },
    // 撤消
    {
        'title': '撤销',
        'action': 'undo',
        'label': '',
        'img': '/graph/images/icon/undo.png',
        'isTransparent': true
    },
    // 还原
    {
        'title': '还原',
        'action': 'redo',
        'label': '',
        'img': '/graph/images/icon/redo.png',
        'isTransparent': true
    },
    // 查看
    {
        'title': '查看',
        'action': 'show',
        'label': '',
        'img': '/graph/images/icon/show.png',
        'isTransparent': true
    },
    // 打印预览
    // {
    //     'title': '打印预览',
    //     'action': 'print',
    //     'label': '',
    //     'img': '/graph/images/icon/print.png',
    //     'isTransparent': true
    // },
    // 放大
    {
        'title': '放大',
        'action': 'zoomIn',
        'label': '',
        'img': '/graph/images/icon/zoomin.png',
        'isTransparent': true
    },
    // 缩小
    {
        'title': '缩小',
        'action': 'zoomOut',
        'label': '',
        'img': '/graph/images/icon/zoomout.png',
        'isTransparent': true
    },
    // 划线
    // {
    //     'title': '划线',
    //     'action': 'curved',
    //     'label': '',
    //     'img': '/graph/images/icon/curve.png',
    //     'isTransparent': true
    // },
    // 保存XML
    {
        'title': '保存',
        'action': 'save',
        'label': '',
        'img': '/graph/images/icon/save.png',
        'isTransparent': true
    },
    // 实际
    // {
    //     'title': '实际大小',
    //     'action': 'actualSize',
    //     'label': '',
    //     'img': '/graph/images/icon/actual_size.png',
    //     'isTransparent': true
    // },
    // 布满
    // {
    //     'title': '布满画面',
    //     'action': 'fit',
    //     'label': '',
    //     'img': '/graph/images/icon/fit_size.png',
    //     'isTransparent': true
    // }
];
