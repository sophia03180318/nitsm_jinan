

// 程序启动方法
myGraphEditorUI.prototype.refresh = function(sizeDidChange) {
	sizeDidChange = (sizeDidChange != null) ? sizeDidChange : true;
	var quirks = mxClient.IS_IE && (document.documentMode == null || document.documentMode == 5);
	var w = this.container.clientWidth;
	var h = this.container.clientHeight;
	if (this.container == document.body) {
		w = document.body.clientWidth || document.documentElement.clientWidth;
		h = (quirks) ? document.body.clientHeight || document.documentElement.clientHeight : document.documentElement.clientHeight;
	}
	var off = 0;
	this.diagramContainer.style = 'right:40px; left: 350px; top: 5px; bottom: 28px; touch-action: none; overflow: auto;	'
};
var treeNodes = ''
Graph.prototype.getPageSize = function() {
	return (this.pageVisible) ? new mxRectangle(0, 0, 1447 * this.pageScale,
		330 * this.pageScale) : this.scrollTileSize;
};
getAssetImages()

/**
 * Function: createGroup
 *
 * Creates and returns a clone of <defaultGroup> to be used
 * as a new group cell in <group>.
 */
myGraphEditor.prototype.createGroup = function() {
	var model = this.graph.getModel();
	return model.cloneCell(this.defaultGroup);
};

(function() {
	var editor = new myGraphEditor(false);
	// 底部按钮
	var bottomButtons = [
		// 删除
		{
			'title': '删除(快捷键Delete)',
			'action': 'delete',
			'label': '',
			'img': '/graph/images/icon/delete.png',
			'select': false,
			'isTransparent': true
		},
		// 撤消
		{
			'title': '撤销',
			'action': 'undo',
			'label': '',
			'img': '/graph/images/icon/undo.png',
			'select': false,
			'isTransparent': true
		},
		// 还原
		{
			'title': '还原',
			'action': 'redo',
			'label': '',
			'img': '/graph/images/icon/redo.png',
			'select': false,
			'isTransparent': true
		},
		// 放大
		{
			'title': '放大',
			'action': 'zoomIn',
			'label': '',
			'img': '/graph/images/icon/zoomin.png',
			'select': false,
			'isTransparent': true
		},
		// 缩小
		{
			'title': '缩小',
			'action': 'zoomOut',
			'label': '',
			'img': '/graph/images/icon/zoomout.png',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '保存',
			'action': 'save',
			'label': '',
			'img': '/graph/images/icon/save.png',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '(Un)group',
			'action': 'groupOrUngroup',
			'label': '',
			'img': '/graph/images/icon/group.png',
			'select': false,
			'isTransparent': true
		},
		{
			'title': 'rotate',
			'action': 'turn',
			'label': '',
			'img': '/graph/images/handle-rotate.png',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '置于上层',
			'action': 'toFront',
			'label': '',
			'img': '/graph/images/icon/tofront.gif',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '置于下层',
			'action': 'toBack',
			'label': '',
			'img': '/graph/images/icon/toback.gif',
			'select': false,
			'isTransparent': true
		}

	];
	//创建核心展示界面
	myGraphEditorUI.prototype.init = function () {
		var graph = this.editor.graph;
		graph.setAllowDanglingEdges(false);
		//创建页面Div,diagramContainer
		this.createDivs();
		// 禁用浏览器默认的右键菜单栏
		mxEvent.disableContextMenu(this.diagramContainer);
		//初始化graph
		graph.init(this.diagramContainer);
		this.refresh();
		this.editor.resetGraph();
		this.initCanvas();
		graph.sizeDidChange();



		// Global handler to hide the current menu   页面菜单显示隐藏
		this.gestureHandler = mxUtils.bind(this, function(evt)
		{
			this.hideCurrentMenu();
		});
		mxEvent.addGestureListeners(document, this.gestureHandler);
		// 在窗口底部添加按钮
		if (this.bottomButtons != null) {
			status.innerHTML = "";
			for (var i = 0; i < this.bottomButtons.length; i++) {
				var btn = this.bottomButtons[i];
				var action = this.actions.get(btn.action);
				if (action != null) {
					this.addToolbarButton(this.editor, this.editor.editorStatus, action, btn.label,
						btn.img, btn.isTransparent, btn.title, btn.select);
				}
			}
		}
		;
	}




	new myGraphEditorUI(editor, document.getElementById('topoContainer'), 'net_topo', document.getElementById('statusContainer'), bottomButtons);
	var graph = editor.graph;
	graph.setCellsResizable(true);//节点不可改变大小
	var style = graph.getStylesheet().styles.defaultVertex;
	style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_IMAGE //节点背景图片
	style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter
	style[mxConstants.STYLE_LABEL_WIDTH] = 40
	style[mxConstants.STYLE_FONTSIZE] = 10;
	style[mxConstants.STYLE_SPACING_BOTTOM] = '18px'
	style[mxConstants.STYLE_RESIZABLE] = '0'   //不可编辑
	style[mxConstants.STYLE_FONTCOLOR] = '#999' //字体颜色
	style[mxConstants.STYLE_FILLCOLOR] = 'transparent'; //填充色
	style[mxConstants.STYLE_WHITE_SPACE] = 'wrap' //自动换行
	style[mxConstants.STYLE_OVERFLOW] = 'hidden'
	style[mxConstants.STYLE_VERTICAL_LABEL_POSITION] = mxConstants.ALIGN_BOTTOM
	style[mxConstants.STYLE_LABEL_BACKGROUNDCOLOR] = 'transparent' // 连线文字背景颜色
	graph.getStylesheet().putDefaultVertexStyle(style)
	
	var styleGroup = new Object();
	styleGroup[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_RECTANGLE;
	styleGroup[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
	styleGroup[mxConstants.STYLE_ROUNDED] = false; //圆角
	styleGroup[mxConstants.STYLE_FILLCOLOR] = 'transparent'; //填充色
	styleGroup[mxConstants.STYLE_STROKECOLOR] = '#fdee0b';
	styleGroup[mxConstants.STYLE_STROKEWIDTH] = '2';
	styleGroup[mxConstants.STYLE_FONTSIZE] = '0';
	styleGroup[mxConstants.STYLE_RESIZABLE] = '0' 
	graph.getStylesheet().putCellStyle('group', styleGroup);

	var bgStyle = new Object();
	bgStyle[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_IMAGE
	bgStyle[mxConstants.STYLE_RESIZABLE] = '0'
	bgStyle[mxConstants.STYLE_FONTSIZE] = '0';
	graph.getStylesheet().putCellStyle('bgStyle', bgStyle);
	//设置默认组
	//groupBorderSize
	//设置图形和它的子元素的边距
	var group = new mxCell('Group', new mxGeometry(), 'group');
	group.setVertex(true);
	group.setConnectable(false);
	editor.defaultGroup = group;
	editor.groupBorderSize = 8;
	 mxGraphHandler.prototype.guidesEnabled = true;//显示细胞位置标尺
	// 组织结构树
	initOrgTree(graph, editor);

	//插入面板
	$('#assetImages').on('click','li',function(type){
		var category = $(this).attr("id");   //获取到的id 命名为型号,和image 的名字统一
		var oHeight = $(this).attr("data-height")
		// viewGraph(graph, editor, category);
			var width = 1200;
			var height = 120 * parseInt(oHeight);
		
		var style = graph.getStylesheet().styles.bgStyle;
		style.image = '/graph/images/picture/' + category + '.jpg';
		var cells = [new mxCell('', new mxGeometry(0, 0, width, height), 'bgStyle','',category)];

		cells[0].vertex = true;
		cells[0].setConnectable(false);
		

		cells = graph.getImportableCells(cells);
		graph.stopEditing();
		if (!graph.isCellLocked(graph.getDefaultParent())) {
			graph.model.beginUpdate();
			var pt = graph.getCenterInsertPoint(graph.getBoundingBoxFromGeometry(cells, true));
			//x = Math.round(pt.x);
			// y = Math.round(pt.y);
			x = 10,
				y = 10,
				select = graph.importCells(cells, x, y, null);
			// Executes parent layout hooks for position/order
			if (graph.layoutManager != null) {
				var layout = graph.layoutManager.getLayout(null);
				if (layout != null) {
					var s = graph.view.scale;
					var tr = graph.view.translate;
					var tx = (x + tr.x) * s;
					var ty = (y + tr.y) * s;
					for (var i = 0; i < select.length; i++) {
						layout.moveCell(select[i], tx, ty);
					}
				}
			}
			graph.model.endUpdate();
			if (select != null && select.length > 0) {
				graph.scrollCellToVisible(select[0]);
				graph.setSelectionCells(select);
			}
		}
		
		graph.orderCells(true);

	});
	graph.addListener(mxEvent.DOUBLE_CLICK, function(sender, evt){
		const cell = evt.getProperty("cell");
		console.log(sender)
		console.log(cell)
		const portMode= cell.portMode
		console.log(portMode)
		if(cell.assetType==='net_topo'
		){
			console.log('点击了节点')
			var content = '<select name="portIndex"  style="width:100%" id="portMode" lay-filter="portMode">';
			content = content + '<option '+(portMode==1?'selected = "selected"':'' )+' value=\"' + 1 + '\">' +
				'电口' +
			(portMode==1?'[已配置]':'' )
				+
				'</option>';
			content = content + '<option  '+(portMode==2?'selected = "selected"':'' )+' value=\"' + 2 + '\">' +
				'光口' +
			(portMode==2?'[已配置]':'' )
				+
				'</option>';
			content = content + '</select>';
			layer.open({
				title: '请选择光电口'
				, btn: ['确定']
				, content: content,
				yes: function (index, layero) {
					var vlanName = $('#portMode').val();
					var model = graph.getModel();
					console.log(vlanName)
					console.log(model)
					// model.beginUpdate();
					// try {
					cell.setPortMode(vlanName);
					// 	graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, 'yellow', [cell]);
					// 	//   cell.setStyle("fontSize=9;shape=rect;resizable=0;strokeColor=#000000;fillColor=yellow");
					// } finally {
					// 	model.endUpdate();
					// }
					layer.close(index);
				}
			});
		}
	})
})();
mxKeyHandler.prototype.isGraphEvent = function(evt) {
	return true;
};

Action.prototype.saveGraph = function(editor) {


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
			chkboxType: { "Y": "", "N": "" }
		},
		callback: {
			onClick: function(event, treeId, treeNode) {
				treeNodes = treeNode
				var assetType = treeNode.name.slice(treeNode.name.indexOf('(') + 1 ,treeNode.name.indexOf(')'))
				if (treeNode.type == 201 || treeNode.type == 42) {
					viewGraph(graph, editor, treeNode.id,assetType);
				};
			}
		}
	};
	$.get($("#orgTree").data("url"), function(result) {
		var zNodes = [];
		result.data.forEach(function(item) {
			var org = {
				id: item.id,
				pId: item.pid,
				name: item.title,
				icon: (item.type == 201 || item.type == 42) ? (item.type == 201 ? "/graph/images/switch.png" : "/graph/images/router.png") : null,
				type: item.type
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

// 图形回显
function viewGraph(graph, editor, assetId,assetImage) {
	// 清空画布
	editor.graph.model.clear();
	editor.resetGraph();
	editor.fireEvent(new mxEventObject('resetGraphView'));
	// 在一个事务中添加所有cell到model中
	graph.getModel().beginUpdate();
	graph.clickAssetId = assetId;

	try {
		$.ajax({
			url: "/system/graphInterface/listPort",
			type: "post",
			data: assetId,
			contentType: 'application/json;charset=utf-8',
			success: function(result) {
				if (result.data) {
					var picData = result.data["pic"];
					showPic(picData, editor, graph);

					if (result.data["vlan"]) {
						var model = graph.getModel();
						model.beginUpdate();
						var vlans = result.data["vlan"];

						try {
							for (var i = 0; i < vlans.length; i++) {
								var xmlDoc = mxUtils.parseXml(vlans[i].contentStr);
								var codec = new mxCodec(xmlDoc);
								var group = new mxCell('Group', new mxGeometry(), 'group');
								group.setVertex(true);
								group.setConnectable(false);
								var vlan = codec.decode(xmlDoc.documentElement, group);
								graph.addCell(vlan);
							}

						} finally {
							model.endUpdate();
						}
					}
					


					//测试使用手动添加
					var parent = graph.getDefaultParent();
					var data = result.data["port"];
					if (data != null && data.length > 0) {//开始构造设备节点
						showNodes(data, editor, graph, parent);
					}
				}

				$('#topoContainer').trigger('click');
			}

		});
	} finally {
		// 更新图
		graph.getModel().endUpdate();
	}
}
function showPic(vertexs, editor, graph){
	var cells = editor.graph.getModel().cells;
	for (var i = 0; i < vertexs.length; i++) {
		var vertex = vertexs[i];
		var id = vertex.nodeId;
		var category = vertex.categoryPic
		var assetPost = 'bgStyle'
		var picName = vertex.pictureImg
		var assetId = vertex.assetId;
		var value = vertex.pictureImg;
		var style = 'image=/graph/images/picture/' + category + '.jpg';
		var	x = vertex.pointX;
		var	y = vertex.pointY;
	    var width = vertex.width;
	    var height = vertex.height;
		var relative = false;
		var connectable = false;
		var offset = null;
		
		graph.createVertexAsset(cells[vertex.parentId], id, value, x, y, width, height, style, relative, assetId, category, connectable, offset, assetPost);
		graph.setCellStyles(mxConstants.STYLE_FONTSIZE,'0',cells)
		
	}
}
//显示节点
function showNodes(vertexs, editor, graph, parent) {
	var cells = editor.graph.getModel().cells;
	for (var i = 0; i < vertexs.length; i++) {
		var vertex = vertexs[i];
		console.log(vertex)
		var id = vertex.nodeId;
		var value = vertex.portName;
		var x = 10;
		var y = 140;
		var rotate = vertex.rotation ? vertex.rotation : 0;
		var portMode = vertex.portMode || '1'
		var style = "";
		if (i % 2 == 0) {
			x = vertex.pointX == null ? 42 * (i / 2) + x : vertex.pointX;
			y = vertex.pointY == null ? 0 + y : vertex.pointY;
			if(portMode=='1'){
				style = "image=/graph/images/picture/port-old.png;rotation=" + rotate;
			}else{
				style = "image=/graph/images/picture/port-guang.png;rotation=" + rotate;
			}
		} else {
			x = vertex.pointX == null ? 42 * ((i - 1) / 2) + x : vertex.pointX;
			y = vertex.pointY == null ? 35 + y : vertex.pointY;
			if(portMode=='1'){
				style = "image=/graph/images/picture/port-old.png;rotation=" + rotate;
			}else{
				style = "image=/graph/images/picture/port-guang.png;rotation=" + rotate;
			}
		}
		var width = 28
		var height = 25
		var assetId = vertex.portIndex;
		var assetType = editor.category;
		var relative = false;
		var connectable = false;
		var offset = null;
		var isPort = vertex.isPort;

		if (vertex.parentId != null) {
			graph.createVertexAsset(cells[vertex.parentId], id, value, x, y, width, height, style, relative, assetId, assetType, connectable, offset, isPort, null, portMode);
		} else {
			graph.createVertexAsset(parent, id, value, x, y, width, height, style, relative, assetId, assetType, connectable, offset, isPort, null, portMode);
		}
		


	}
} Action.prototype.saveGraph = function(editor) {
	var nodes = [];
	var cells = editor.graph.getModel().cells;
	for (var key in cells) {
		var cell = cells[key];
		if (cell.assetType == "net_topo") {
			var saveNode = new Object();
			saveNode.assetId = editor.graph.clickAssetId;
			saveNode.portIndex = cell.assetId;
			saveNode.nodeId = cell.id;
			saveNode.pointX = cell.geometry.x;
			saveNode.pointY = cell.geometry.y;
			saveNode.portMode = cell.portMode;
			//saveNode.rotation = cell.geometry.transform
			saveNode.type = "net_topo";
			if (cell.parent != null && cell.parent.style != null ) {
				if(cell.parent.style.indexOf("group")!= '-1'){
					saveNode.parentId = cell.parent.id;
				}
			}
			if(cell.style.indexOf("rotation=") != '-1'){
				var rotation = cell.style.slice(cell.style.lastIndexOf("rotation=") + 9,cell.style.length)
				var rotVal = null
				if(rotation.indexOf(';') != '-1'){
					rotVal = cell.style.slice(cell.style.lastIndexOf("rotation=") + 9,cell.style.length - 1)
				}else{
					rotVal = rotation
				}
				saveNode.rotation = rotVal
			}
			console.log(saveNode)
			nodes.push(saveNode);
		} else if (cell.style != null ) {  //group
			if(cell.style.indexOf("group")!= '-1'){
				var enc = new mxCodec(mxUtils.createXmlDocument());
				var node = enc.encode(cell);
				var value = mxUtils.getXml(node);
				var saveVlanNode = new Object();
				saveVlanNode.assetId = editor.graph.clickAssetId;
				saveVlanNode.content = value;
				saveVlanNode.nodeId = cell.id;
				saveVlanNode.pointX = cell.geometry.x;
				saveVlanNode.pointY = cell.geometry.y;
				saveVlanNode.width = cell.geometry.width;
				saveVlanNode.height = cell.geometry.height;
				saveVlanNode.type = "group";
				console.log(saveVlanNode)

				nodes.push(saveVlanNode);
			}else if ((cell.style != null && cell.style == "bgStyle") || cell.isPort == "bgStyle") {
				var enc = new mxCodec(mxUtils.createXmlDocument());
				var node = enc.encode(cell);
				var value = mxUtils.getXml(node);
				var savePicutreNode = new Object();
				savePicutreNode.assetId = editor.graph.clickAssetId;
				savePicutreNode.content = value;
				savePicutreNode.categoryPic = cell.assetType;
				savePicutreNode.pictureImg = cell.value;

				savePicutreNode.nodeId = cell.id;
				savePicutreNode.pointX = cell.geometry.x;
				savePicutreNode.pointY = cell.geometry.y;
				savePicutreNode.width = cell.geometry.width;
				savePicutreNode.height = cell.geometry.height;
				savePicutreNode.type = "picture";
				nodes.push(savePicutreNode);
			}
	   	}

	}
	var v = { "assetId": editor.graph.clickAssetId, "topoNodePortVos": nodes };
	$.ajax({
		url: "/system/graphInterface/save",
		type: "post",
		data: JSON.stringify(v),
		contentType: 'application/json;charset=utf-8',
		success: function(result) {
			if (result.msg) {
				layer.alert(result.msg);

				viewGraph(editor.graph, editor,editor.graph.clickAssetId,treeNodes);

			} else {
				layer.alert("权限不足");
			}
		}
	});
};

function getAssetImages(){
	var assetImageList = []
	$.ajax({
		url: "/api/free/getImageList?model=201",
		type: "post",
		contentType: 'application/json;charset=utf-8',
		success: function(result) {
			assetImageList = result.data;
			$.ajax({
				url: "/api/free/getImageList?model=42",
				type: "post",
				contentType: 'application/json;charset=utf-8',
				success: function(result) {
					var assetImageLists = assetImageList.concat(result.data);
					if (assetImageLists != null && assetImageLists.length > 0) {
						var contents = "";
						var list = [];
						assetImageLists.forEach((elm)=> {
							if( elm == 'c9404' ){
								list.push({assetImage:elm ,assetHeight:4})
							}else if(elm == 'CISCO3845C' || elm == 'CISCO7200' ||  elm == 'CISCO3925' || elm == 'CISCO3900' ){
								list.push({assetImage:elm ,assetHeight:3})
							}else if(elm == 'CISCO3800'   || elm == 'CISCO3945'){
								list.push({assetImage:elm ,assetHeight:2})
							}else if(elm == 'CISCO4506' ){
								list.push({assetImage:elm ,assetHeight:10})
							}else if(elm == 'CISCON7K'){
								list.push({assetImage:elm ,assetHeight:20})
							}else if(elm == 'ASR1006'){
								list.push({assetImage:elm ,assetHeight:6})
							}else{
								list.push({assetImage:elm ,assetHeight:1})
							}
						})
						list.forEach(elm => {
							contents += `<li class='type' style="cursor:pointer;" id='${elm.assetImage}' data-height='${elm.assetHeight}'>
											<div class='type-name'>${elm.assetImage} (${elm.assetHeight}U)</div>
					                		<div class='type-item-img'>
					                			<img src='../../graph/images/picture/${elm.assetImage}.jpg'>
					                		</div>
				                	    	</li>`;							
						})
						$("#assetImages").html(contents);
					} else {
			
						var content = "<li>暂无设备型号</li>";
						$("#assetImages").html(content);
					}
				}
			});
		}
	});
}









