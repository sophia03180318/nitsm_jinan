/**
 * Copyright (c) 2006-2017, JGraph Ltd
 * Copyright (c) 2006-2017, Gaudenz Alder
 */
var mxClient =
    {
        /**
         * Class: mxClient
         *
         * Bootstrapping mechanism for the mxGraph thin client. The production version
         * of this file contains all code required to run the mxGraph thin client, as
         * well as global constants to identify the browser and operating system in
         * use. You may have to load chrome://global/content/contentAreaUtils.js in
         * your page to disable certain security restrictions in Mozilla.
         *
         * Variable: VERSION
         *
         * Contains the current version of the mxGraph library. The strings that
         * communicate versions of mxGraph use the following format.
         *
         * versionMajor.versionMinor.buildNumber.revisionNumber
         *
         * Current version is 4.1.0.
         */
        VERSION: '4.1.0',

        /**
         * Variable: IS_IE
         *
         * True if the current browser is Internet Explorer 10 or below. Use <mxClient.IS_IE11>
         * to detect IE 11.
         */
        // IE浏览器
        IS_IE: navigator.userAgent.indexOf('MSIE') >= 0,

        /**
         * Variable: IS_IE6
         *
         * True if the current browser is Internet Explorer 6.x.
         */
        IS_IE6: navigator.userAgent.indexOf('MSIE 6') >= 0,

        /**
         * Variable: IS_IE11
         *
         * True if the current browser is Internet Explorer 11.x.
         */
        IS_IE11: !!navigator.userAgent.match(/Trident\/7\./),

        /**
         * Variable: IS_EDGE
         *
         * True if the current browser is Microsoft Edge.
         */
        // Edge浏览器
        IS_EDGE: !!navigator.userAgent.match(/Edge\//),

        /**
         * Variable: IS_QUIRKS
         *
         * True if the current browser is Internet Explorer and it is in quirks mode.
         */
        // 如果是IE浏览器，标识是否是Quirks模式
        IS_QUIRKS: navigator.userAgent.indexOf('MSIE') >= 0 && (document.documentMode == null || document.documentMode == 5),

        /**
         * Variable: IS_EM
         *
         * True if the browser is IE11 in enterprise mode (IE8 standards mode).
         */
        // IE11的enterprise模式（IE8的standards模式）
        IS_EM: 'spellcheck' in document.createElement('textarea') && document.documentMode == 8,

        /**
         * Variable: VML_PREFIX
         *
         * Prefix for VML namespace in node names. Default is 'v'.
         */
        // VML命名空间中结点的前缀，默认为v
        VML_PREFIX: 'v',

        /**
         * Variable: OFFICE_PREFIX
         *
         * Prefix for VML office namespace in node names. Default is 'o'.
         */
        // VML office命令空间中结点的前缀，默认为o
        OFFICE_PREFIX: 'o',

        /**
         * Variable: IS_NS
         *
         * True if the current browser is Netscape (including Firefox).
         */
        // Netscape浏览器（包括Firefox浏览器）
        IS_NS: navigator.userAgent.indexOf('Mozilla/') >= 0 &&
            navigator.userAgent.indexOf('MSIE') < 0 &&
            navigator.userAgent.indexOf('Edge/') < 0,

        /**
         * Variable: IS_OP
         *
         * True if the current browser is Opera.
         */
        // Opera浏览器
        IS_OP: navigator.userAgent.indexOf('Opera/') >= 0 ||
            navigator.userAgent.indexOf('OPR/') >= 0,

        /**
         * Variable: IS_OT
         *
         * True if -o-transform is available as a CSS style, ie for Opera browsers
         * based on a Presto engine with version 2.5 or later.
         */
        // 如果-o-transform可用作CSS样式，则为True，即基于具有2.5或更高版本的Presto引擎的Opera浏览器。
        IS_OT: navigator.userAgent.indexOf('Presto/') >= 0 &&
            navigator.userAgent.indexOf('Presto/2.4.') < 0 &&
            navigator.userAgent.indexOf('Presto/2.3.') < 0 &&
            navigator.userAgent.indexOf('Presto/2.2.') < 0 &&
            navigator.userAgent.indexOf('Presto/2.1.') < 0 &&
            navigator.userAgent.indexOf('Presto/2.0.') < 0 &&
            navigator.userAgent.indexOf('Presto/1.') < 0,

        /**
         * Variable: IS_SF
         *
         * True if the current browser is Safari.
         */
        // Safari浏览器
        IS_SF: navigator.userAgent.indexOf('AppleWebKit/') >= 0 &&
            navigator.userAgent.indexOf('Chrome/') < 0 &&
            navigator.userAgent.indexOf('Edge/') < 0,

        /**
         * Variable: IS_ANDROID
         *
         * Returns true if the user agent contains Android.
         */
        IS_ANDROID: navigator.userAgent.indexOf('Android') >= 0,

        /**
         * Variable: IS_IOS
         *
         * Returns true if the user agent is an iPad, iPhone or iPod.
         */
        // iPad、iPhone和iPod平台
        IS_IOS: (/iP(hone|od|ad)/.test(navigator.platform)),

        /**
         * Variable: IOS_VERSION
         *
         * Returns the major version number for iOS devices or 0 if the
         * device is not an iOS device.
         */
        IOS_VERSION: (function () {
            if ((/iP(hone|od|ad)/.test(navigator.platform))) {
                var v = (navigator.appVersion).match(/OS (\d+)_(\d+)_?(\d+)?/);

                if (v != null && v.length > 0) {
                    return parseInt(v[1]);
                }
            }

            return 0;
        })(),

        /**
         * Variable: IS_GC
         *
         * True if the current browser is Google Chrome.
         */
        // Google Chrome浏览器
        IS_GC: navigator.userAgent.indexOf('Chrome/') >= 0 &&
            navigator.userAgent.indexOf('Edge/') < 0,

        /**
         * Variable: IS_CHROMEAPP
         *
         * True if the this is running inside a Chrome App.
         */
        // Chrome App
        IS_CHROMEAPP: window.chrome != null && chrome.app != null && chrome.app.runtime != null,

        /**
         * Variable: IS_FF
         *
         * True if the current browser is Firefox.
         */
        // Firefox浏览器
        IS_FF: navigator.userAgent.indexOf('Firefox/') >= 0,

        /**
         * Variable: IS_MT
         *
         * True if -moz-transform is available as a CSS style. This is the case
         * for all Firefox-based browsers newer than or equal 3, such as Camino,
         * Iceweasel, Seamonkey and Iceape.
         */
        // 如果-moz-transform可用作CSS样式，则为True。
        // 所有基于Firefox的浏览器都是3或者等于3的情况，例如Camino，Iceweasel，Seamonkey和Iceape。
        IS_MT: (navigator.userAgent.indexOf('Firefox/') >= 0 &&
            navigator.userAgent.indexOf('Firefox/1.') < 0 &&
            navigator.userAgent.indexOf('Firefox/2.') < 0) ||
            (navigator.userAgent.indexOf('Iceweasel/') >= 0 &&
                navigator.userAgent.indexOf('Iceweasel/1.') < 0 &&
                navigator.userAgent.indexOf('Iceweasel/2.') < 0) ||
            (navigator.userAgent.indexOf('SeaMonkey/') >= 0 &&
                navigator.userAgent.indexOf('SeaMonkey/1.') < 0) ||
            (navigator.userAgent.indexOf('Iceape/') >= 0 &&
                navigator.userAgent.indexOf('Iceape/1.') < 0),

        /**
         * Variable: IS_VML
         *
         * True if the browser supports VML.
         */
        IS_VML: navigator.appName.toUpperCase() == 'MICROSOFT INTERNET EXPLORER',

        /**
         * Variable: IS_SVG
         *
         * True if the browser supports SVG.
         */
        // 浏览器是否支持SVG
        IS_SVG: navigator.appName.toUpperCase() != 'MICROSOFT INTERNET EXPLORER',

        /**
         * Variable: NO_FO
         *
         * True if foreignObject support is not available. This is the case for
         * Opera, older SVG-based browsers and all versions of IE.
         */
        // 如果foreignObject支持不可用，则为True。 这是Opera，较旧的基于SVG的浏览器和所有版本的IE的情况。
        NO_FO: !document.createElementNS || document.createElementNS('http://www.w3.org/2000/svg',
            'foreignObject') != '[object SVGForeignObjectElement]' || navigator.userAgent.indexOf('Opera/') >= 0,

        /**
         * Variable: IS_WIN
         *
         * True if the client is a Windows.
         */
        // Windows系统
        IS_WIN: navigator.appVersion.indexOf('Win') > 0,

        /**
         * Variable: IS_MAC
         *
         * True if the client is a Mac.
         */
        // Mac系统
        IS_MAC: navigator.appVersion.indexOf('Mac') > 0,

        /**
         * Variable: IS_CHROMEOS
         *
         * True if the client is a Chrome OS.
         */
        IS_CHROMEOS: /\bCrOS\b/.test(navigator.userAgent),

        /**
         * Variable: IS_TOUCH
         *
         * True if this device supports touchstart/-move/-end events (Apple iOS,
         * Android, Chromebook and Chrome Browser on touch-enabled devices).
         */
        // 如果此设备支持touchstart/-move/-end事件，
        // 启用触摸的设备上的Apple iOS，Android，Chromebook和Chrome浏览器，则为True。
        IS_TOUCH: 'ontouchstart' in document.documentElement,

        /**
         * Variable: IS_POINTER
         *
         * True if this device supports Microsoft pointer events (always false on Macs).
         */
        // 如果此设备支持Microsoft指针事件，则为True（在Mac上始终为false）。
        IS_POINTER: window.PointerEvent != null && !(navigator.appVersion.indexOf('Mac') > 0),

        /**
         * Variable: IS_LOCAL
         *
         * True if the documents location does not start with http:// or https://.
         */
        // 是否是本地运行（如果文档位置不以http：//或https：//开头，则为True）。
        IS_LOCAL: document.location.href.indexOf('http://') < 0 &&
            document.location.href.indexOf('https://') < 0,

        /**
         * Variable: defaultBundles
         *
         * Contains the base names of the default bundles if mxLoadResources is false.
         */
        defaultBundles: [],

        /**
         * Function: isBrowserSupported
         *
         * Returns true if the current browser is supported, that is, if
         * <mxClient.IS_VML> or <mxClient.IS_SVG> is true.
         *
         * Example:
         *
         * (code)
         * if (!mxClient.isBrowserSupported())
         * {
         *   mxUtils.error('Browser is not supported!', 200, false);
         * }
         * (end)
         */
        isBrowserSupported: function () {
            return mxClient.IS_VML || mxClient.IS_SVG;
        },

        /**
         * Function: link
         *
         * Adds a link node to the head of the document. Use this
         * to add a stylesheet to the page as follows:
         *
         * (code)
         * mxClient.link('stylesheet', filename);
         * (end)
         *
         * where filename is the (relative) URL of the stylesheet. The charset
         * is hardcoded to ISO-8859-1 and the type is text/css.
         *
         * Parameters:
         *
         * rel - String that represents the rel attribute of the link node.
         * href - String that represents the href attribute of the link node.
         * doc - Optional parent document of the link node.
         * id - unique id for the link element to check if it already exists
         */
        /**
         * rel：link标签的rel属性
         * href：link标签的href属性，相对路径
         * doc：可选的link标签所属的document
         */
        link: function (rel, href, doc, id) {
            doc = doc || document;

            // Workaround for Operation Aborted in IE6 if base tag is used in head
            // 如果是IE6则直接在head中添加link标签
            if (mxClient.IS_IE6) {
                doc.write('<link rel="' + rel + '" href="' + href + '" charset="UTF-8" type="text/css"/>');
            } else {
                var link = doc.createElement('link');

                link.setAttribute('rel', rel);
                link.setAttribute('href', href);
                link.setAttribute('charset', 'UTF-8');
                link.setAttribute('type', 'text/css');

                if (id) {
                    link.setAttribute('id', id);
                }

                var head = doc.getElementsByTagName('head')[0];
                head.appendChild(link);
            }
        },

        /**
         * Function: loadResources
         *
         * Helper method to load the default bundles if mxLoadResources is false.
         *
         * Parameters:
         *
         * fn - Function to call after all resources have been loaded.
         * lan - Optional string to pass to <mxResources.add>.
         */
        /**
         * fn：在所有资源文件加载之后调用的函数
         * lan：可选的传递给<mxResources.add>的参数
         */
        loadResources: function (fn, lan) {
            var pending = mxClient.defaultBundles.length;

            // 如果没有需要加载的资源文件，则直接调用fn
            function callback() {
                if (--pending == 0) {
                    fn();
                }
            }

            // 使用<mxResources.add>加载资源文件
            for (var i = 0; i < mxClient.defaultBundles.length; i++) {
                mxResources.add(mxClient.defaultBundles[i], lan, callback);
            }
        },

        /**
         * Function: include
         *
         * Dynamically adds a script node to the document header.
         *
         * In production environments, the includes are resolved in the mxClient.js
         * file to reduce the number of requests required for client startup. This
         * function should only be used in development environments, but not in
         * production systems.
         */
        /**
         *     src：script标签的src属性，相对路径
         */
        include: function (src) {
            document.write('<script src="' + src + '"></script>');
        }
    };

/**
 * Detects desktop mode on iPad Pro which should block event handling like iOS 12.
 */
if (mxClient.IS_SF && mxClient.IS_TOUCH && !mxClient.IS_IOS) {
    mxClient.IOS_VERSION = 13;
    mxClient.IOS = true;
}

/**
 * Variable: mxLoadResources
 *
 * Optional global config variable to toggle loading of the two resource files
 * in <mxGraph> and <mxEditor>. Default is true. NOTE: This is a global variable,
 * not a variable of mxClient. If this is false, you can use <mxClient.loadResources>
 * with its callback to load the default bundles asynchronously.
 *
 * (code)
 * <script type="text/javascript">
 *        var mxLoadResources = false;
 * </script>
 * <script type="text/javascript" src="/path/to/core/directory/js/mxClient.js"></script>
 * (end)
 */
// 用于切换<mxGraph> 和 <mxEditor>中两个资源文件的加载
// 这两个资源文件在resource资源文件夹中，如果应用已经提供则置false，参照GraphEditor
if (typeof (mxLoadResources) == 'undefined') {
    mxLoadResources = true;
}

/**
 * Variable: mxForceIncludes
 *
 * Optional global config variable to force loading the JavaScript files in
 * development mode. Default is undefined. NOTE: This is a global variable,
 * not a variable of mxClient.
 *
 * (code)
 * <script type="text/javascript">
 *        var mxLoadResources = true;
 * </script>
 * <script type="text/javascript" src="/path/to/core/directory/js/mxClient.js"></script>
 * (end)
 */
// 用于强制在开发模式下加载JavaScript文件。默认为undefined
if (typeof (mxForceIncludes) == 'undefined') {
    mxForceIncludes = false;
}

/**
 * Variable: mxResourceExtension
 *
 * Optional global config variable to specify the extension of resource files.
 * Default is true. NOTE: This is a global variable, not a variable of mxClient.
 *
 * (code)
 * <script type="text/javascript">
 *        var mxResourceExtension = '.txt';
 * </script>
 * <script type="text/javascript" src="/path/to/core/directory/js/mxClient.js"></script>
 * (end)
 */
// 用于指定资源文件的后缀。默认为.txt
if (typeof (mxResourceExtension) == 'undefined') {
    mxResourceExtension = '.txt';
}

/**
 * Variable: mxLoadStylesheets
 *
 * Optional global config variable to toggle loading of the CSS files when
 * the library is initialized. Default is true. NOTE: This is a global variable,
 * not a variable of mxClient.
 *
 * (code)
 * <script type="text/javascript">
 *        var mxLoadStylesheets = false;
 * </script>
 * <script type="text/javascript" src="/path/to/core/directory/js/mxClient.js"></script>
 * (end)
 */
// 用于在初始化库时切换加载CSS文件
if (typeof (mxLoadStylesheets) == 'undefined') {
    mxLoadStylesheets = true;
}

/**
 * Variable: basePath
 *
 * Basepath for all URLs in the core without trailing slash. Default is '.'.
 * Set mxBasePath prior to loading the mxClient library as follows to override
 * this setting:
 *
 * (code)
 * <script type="text/javascript">
 *        mxBasePath = '/path/to/core/directory';
 * </script>
 * <script type="text/javascript" src="/path/to/core/directory/js/mxClient.js"></script>
 * (end)
 *
 * When using a relative path, the path is relative to the URL of the page that
 * contains the assignment. Trailing slashes are automatically removed.
 */
// 所有文件的基本路径，不带斜杠。默认为'.'
// 在加载mxClient库之前覆盖此设置，比如mxBasePath='/path/to/core/directory'
if (typeof (mxBasePath) != 'undefined' && mxBasePath.length > 0) {
    // Adds a trailing slash if required
    if (mxBasePath.substring(mxBasePath.length - 1) == '/') {
        mxBasePath = mxBasePath.substring(0, mxBasePath.length - 1);
    }

    mxClient.basePath = mxBasePath;
} else {
    mxClient.basePath = '.';
}

/**
 * Variable: imageBasePath
 *
 * Basepath for all images URLs in the core without trailing slash. Default is
 * <mxClient.basePath> + '/images'. Set mxImageBasePath prior to loading the
 * mxClient library as follows to override this setting:
 *
 * (code)
 * <script type="text/javascript">
 *        mxImageBasePath = '/path/to/image/directory';
 * </script>
 * <script type="text/javascript" src="/path/to/core/directory/js/mxClient.js"></script>
 * (end)
 *
 * When using a relative path, the path is relative to the URL of the page that
 * contains the assignment. Trailing slashes are automatically removed.
 */
// 所有图片的基本路径，不带斜杠。默认为<mxClient.basePath> + '/images'
// 在加载mxClient库之前覆盖此设置，比如mxImageBasePath='/path/to/image/directory'
if (typeof (mxImageBasePath) != 'undefined' && mxImageBasePath.length > 0) {
    // Adds a trailing slash if required
    if (mxImageBasePath.substring(mxImageBasePath.length - 1) == '/') {
        mxImageBasePath = mxImageBasePath.substring(0, mxImageBasePath.length - 1);
    }

    mxClient.imageBasePath = mxImageBasePath;
} else {
    mxClient.imageBasePath = mxClient.basePath + '/images';
}

/**
 * Variable: language
 *
 * Defines the language of the client, eg. en for english, de for german etc.
 * The special value 'none' will disable all built-in internationalization and
 * resource loading. See <mxResources.getSpecialBundle> for handling identifiers
 * with and without a dash.
 *
 * Set mxLanguage prior to loading the mxClient library as follows to override
 * this setting:
 *
 * (code)
 * <script type="text/javascript">
 *        mxLanguage = 'en';
 * </script>
 * <script type="text/javascript" src="js/mxClient.js"></script>
 * (end)
 *
 * If internationalization is disabled, then the following variables should be
 * overridden to reflect the current language of the system. These variables are
 * cleared when i18n is disabled.
 * <mxEditor.askZoomResource>, <mxEditor.lastSavedResource>,
 * <mxEditor.currentFileResource>, <mxEditor.propertiesResource>,
 * <mxEditor.tasksResource>, <mxEditor.helpResource>, <mxEditor.outlineResource>,
 * <mxElbowEdgeHandler.doubleClickOrientationResource>, <mxUtils.errorResource>,
 * <mxUtils.closeResource>, <mxGraphSelectionModel.doneResource>,
 * <mxGraphSelectionModel.updatingSelectionResource>, <mxGraphView.doneResource>,
 * <mxGraphView.updatingDocumentResource>, <mxCellRenderer.collapseExpandResource>,
 * <mxGraph.containsValidationErrorsResource> and
 * <mxGraph.alreadyConnectedResource>.
 */
// 设置客户端的语言，比如en表示英语，de表示德语。默认是en
// 资源文件的格式是使用下划线表示语言，比如graph_zh.txt
// 在加载mxClient库之前覆盖此设置，比如mxLanguage='en'
if (typeof (mxLanguage) != 'undefined' && mxLanguage != null) {
    mxClient.language = mxLanguage;
} else {
    mxClient.language = (mxClient.IS_IE) ? navigator.userLanguage : navigator.language;
}

/**
 * Variable: defaultLanguage
 *
 * Defines the default language which is used in the common resource files. Any
 * resources for this language will only load the common resource file, but not
 * the language-specific resource file. Default is 'en'.
 *
 * Set mxDefaultLanguage prior to loading the mxClient library as follows to override
 * this setting:
 *
 * (code)
 * <script type="text/javascript">
 *        mxDefaultLanguage = 'de';
 * </script>
 * <script type="text/javascript" src="js/mxClient.js"></script>
 * (end)
 */
// 设置默认语言加载不带下划线的资源文件。默认为en
// 在加载mxClient库之前覆盖此设置，比如mxDefaultLanguage='zh'
if (typeof (mxDefaultLanguage) != 'undefined' && mxDefaultLanguage != null) {
    mxClient.defaultLanguage = mxDefaultLanguage;
} else {
    mxClient.defaultLanguage = 'en';
}

// Adds all required stylesheets and namespaces
if (mxLoadStylesheets) {
    mxClient.link('stylesheet', mxClient.basePath + '/css/common.css');
}

/**
 * Variable: languages
 *
 * Defines the optional array of all supported language extensions. The default
 * language does not have to be part of this list. See
 * <mxResources.isLanguageSupported>.
 *
 * (code)
 * <script type="text/javascript">
 *        mxLanguages = ['de', 'it', 'fr'];
 * </script>
 * <script type="text/javascript" src="js/mxClient.js"></script>
 * (end)
 *
 * This is used to avoid unnecessary requests to language files, ie. if a 404
 * will be returned.
 */
// 设置支持的所有语言。默认语言不用添加到列表中
// 在加载mxClient库之前覆盖此设置，比如mxLanguages=['de','it','fr']
if (typeof (mxLanguages) != 'undefined' && mxLanguages != null) {
    mxClient.languages = mxLanguages;
}

// Adds required namespaces, stylesheets and memory handling for older IE browsers
// 为旧的IE浏览器添加所需的命名空间，样式表和内存处理
if (mxClient.IS_VML) {
    // 如果支持SVG则使用SVG
    if (mxClient.IS_SVG) {
        mxClient.IS_VML = false;
    } else {
        // Enables support for IE8 standards mode. Note that this requires all attributes for VML
        // elements to be set using direct notation, ie. node.attr = value, not setAttribute.
        // 允许支持IE8的standards模式，这需要所有VML属性可以直接赋值，比如node.attr=value
        // 不能使用setAttribute
        if (document.namespaces != null) {
            if (document.documentMode == 8) {
                document.namespaces.add(mxClient.VML_PREFIX, 'urn:schemas-microsoft-com:vml', '#default#VML');
                document.namespaces.add(mxClient.OFFICE_PREFIX, 'urn:schemas-microsoft-com:office:office', '#default#VML');
            } else {
                document.namespaces.add(mxClient.VML_PREFIX, 'urn:schemas-microsoft-com:vml');
                document.namespaces.add(mxClient.OFFICE_PREFIX, 'urn:schemas-microsoft-com:office:office');
            }
        }

        // Workaround for limited number of stylesheets in IE (does not work in standards mode)
        // IE中有限数量样式表的解决方法（在标准模式下不起作用）
        if (mxClient.IS_QUIRKS && document.styleSheets.length >= 30) {
            (function () {
                var node = document.createElement('style');
                node.type = 'text/css';
                node.styleSheet.cssText = mxClient.VML_PREFIX + '\\:*{behavior:url(#default#VML)}' +
                    mxClient.OFFICE_PREFIX + '\\:*{behavior:url(#default#VML)}';
                document.getElementsByTagName('head')[0].appendChild(node);
            })();
        } else {
            document.createStyleSheet().cssText = mxClient.VML_PREFIX + '\\:*{behavior:url(#default#VML)}' +
                mxClient.OFFICE_PREFIX + '\\:*{behavior:url(#default#VML)}';
        }
        // 导入所有样式和命名空间
        if (mxLoadStylesheets) {
            mxClient.link('stylesheet', mxClient.basePath + '/css/explorer.css');
        }
    }
}

// PREPROCESSOR-REMOVE-START
// If script is loaded via CommonJS, do not write <script> tags to the page
// for dependencies. These are already included in the build.
// 如果通过CommonJS加载脚本，不要将<script>标记写入页面以获取依赖项。这些已经包含在编译版本中
if (mxForceIncludes || !(typeof module === 'object' && module.exports != null)) {
// PREPROCESSOR-REMOVE-END
    mxClient.include(mxClient.basePath + '/js/util/mxLog.js');
    mxClient.include(mxClient.basePath + '/js/util/mxObjectIdentity.js');
    mxClient.include(mxClient.basePath + '/js/util/mxDictionary.js');
    mxClient.include(mxClient.basePath + '/js/util/mxResources.js');
    mxClient.include(mxClient.basePath + '/js/util/mxPoint.js');
    mxClient.include(mxClient.basePath + '/js/util/mxRectangle.js');
    mxClient.include(mxClient.basePath + '/js/util/mxEffects.js');
    mxClient.include(mxClient.basePath + '/js/util/mxUtils.js');
    mxClient.include(mxClient.basePath + '/js/util/mxConstants.js');
    mxClient.include(mxClient.basePath + '/js/util/mxEventObject.js');
    mxClient.include(mxClient.basePath + '/js/util/mxMouseEvent.js');
    mxClient.include(mxClient.basePath + '/js/util/mxEventSource.js');
    mxClient.include(mxClient.basePath + '/js/util/mxEvent.js');
    mxClient.include(mxClient.basePath + '/js/util/mxXmlRequest.js');
    mxClient.include(mxClient.basePath + '/js/util/mxClipboard.js');
    mxClient.include(mxClient.basePath + '/js/util/mxWindow.js');
    mxClient.include(mxClient.basePath + '/js/util/mxForm.js');
    mxClient.include(mxClient.basePath + '/js/util/mxImage.js');
    mxClient.include(mxClient.basePath + '/js/util/mxDivResizer.js');
    mxClient.include(mxClient.basePath + '/js/util/mxDragSource.js');
    mxClient.include(mxClient.basePath + '/js/util/mxToolbar.js');
    mxClient.include(mxClient.basePath + '/js/util/mxUndoableEdit.js');
    mxClient.include(mxClient.basePath + '/js/util/mxUndoManager.js');
    mxClient.include(mxClient.basePath + '/js/util/mxUrlConverter.js');
    mxClient.include(mxClient.basePath + '/js/util/mxPanningManager.js');
    mxClient.include(mxClient.basePath + '/js/util/mxPopupMenu.js');
    mxClient.include(mxClient.basePath + '/js/util/mxAutoSaveManager.js');
    mxClient.include(mxClient.basePath + '/js/util/mxAnimation.js');
    mxClient.include(mxClient.basePath + '/js/util/mxMorphing.js');
    mxClient.include(mxClient.basePath + '/js/util/mxImageBundle.js');
    mxClient.include(mxClient.basePath + '/js/util/mxImageExport.js');
    mxClient.include(mxClient.basePath + '/js/util/mxAbstractCanvas2D.js');
    mxClient.include(mxClient.basePath + '/js/util/mxXmlCanvas2D.js');
    mxClient.include(mxClient.basePath + '/js/util/mxSvgCanvas2D.js');
    mxClient.include(mxClient.basePath + '/js/util/mxVmlCanvas2D.js');
    mxClient.include(mxClient.basePath + '/js/util/mxGuide.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxShape.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxStencil.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxStencilRegistry.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxMarker.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxActor.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxCloud.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxRectangleShape.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxEllipse.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxDoubleEllipse.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxRhombus.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxPolyline.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxArrow.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxArrowConnector.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxText.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxTriangle.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxHexagon.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxLine.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxImageShape.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxLabel.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxCylinder.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxConnector.js');
    mxClient.include(mxClient.basePath + '/js/shape/mxSwimlane.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxGraphLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxStackLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxPartitionLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxCompactTreeLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxRadialTreeLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxFastOrganicLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxCircleLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxParallelEdgeLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxCompositeLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/mxEdgeLabelLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/model/mxGraphAbstractHierarchyCell.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/model/mxGraphHierarchyNode.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/model/mxGraphHierarchyEdge.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/model/mxGraphHierarchyModel.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/model/mxSwimlaneModel.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/stage/mxHierarchicalLayoutStage.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/stage/mxMedianHybridCrossingReduction.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/stage/mxMinimumCycleRemover.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/stage/mxCoordinateAssignment.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/stage/mxSwimlaneOrdering.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/mxHierarchicalLayout.js');
    mxClient.include(mxClient.basePath + '/js/layout/hierarchical/mxSwimlaneLayout.js');
    mxClient.include(mxClient.basePath + '/js/model/mxGraphModel.js');
    mxClient.include(mxClient.basePath + '/js/model/mxCell.js');
    mxClient.include(mxClient.basePath + '/js/model/mxGeometry.js');
    mxClient.include(mxClient.basePath + '/js/model/mxCellPath.js');
    mxClient.include(mxClient.basePath + '/js/view/mxPerimeter.js');
    mxClient.include(mxClient.basePath + '/js/view/mxPrintPreview.js');
    mxClient.include(mxClient.basePath + '/js/view/mxStylesheet.js');
    mxClient.include(mxClient.basePath + '/js/view/mxCellState.js');
    mxClient.include(mxClient.basePath + '/js/view/mxGraphSelectionModel.js');
    mxClient.include(mxClient.basePath + '/js/view/mxCellEditor.js');
    mxClient.include(mxClient.basePath + '/js/view/mxCellRenderer.js');
    mxClient.include(mxClient.basePath + '/js/view/mxEdgeStyle.js');
    mxClient.include(mxClient.basePath + '/js/view/mxStyleRegistry.js');
    mxClient.include(mxClient.basePath + '/js/view/mxGraphView.js');
    mxClient.include(mxClient.basePath + '/js/view/mxGraph.js');
    mxClient.include(mxClient.basePath + '/js/view/mxCellOverlay.js');
    mxClient.include(mxClient.basePath + '/js/view/mxOutline.js');
    mxClient.include(mxClient.basePath + '/js/view/mxMultiplicity.js');
    mxClient.include(mxClient.basePath + '/js/view/mxLayoutManager.js');
    mxClient.include(mxClient.basePath + '/js/view/mxSwimlaneManager.js');
    mxClient.include(mxClient.basePath + '/js/view/mxTemporaryCellStates.js');
    mxClient.include(mxClient.basePath + '/js/view/mxCellStatePreview.js');
    mxClient.include(mxClient.basePath + '/js/view/mxConnectionConstraint.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxGraphHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxPanningHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxPopupMenuHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxCellMarker.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxSelectionCellsHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxConnectionHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxConstraintHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxRubberband.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxHandle.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxVertexHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxEdgeHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxElbowEdgeHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxEdgeSegmentHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxKeyHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxTooltipHandler.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxCellTracker.js');
    mxClient.include(mxClient.basePath + '/js/handler/mxCellHighlight.js');
    mxClient.include(mxClient.basePath + '/js/editor/mxDefaultKeyHandler.js');
    mxClient.include(mxClient.basePath + '/js/editor/mxDefaultPopupMenu.js');
    mxClient.include(mxClient.basePath + '/js/editor/mxDefaultToolbar.js');
    mxClient.include(mxClient.basePath + '/js/editor/mxEditor.js');
    mxClient.include(mxClient.basePath + '/js/io/mxCodecRegistry.js');
    mxClient.include(mxClient.basePath + '/js/io/mxCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxObjectCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxCellCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxModelCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxRootChangeCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxChildChangeCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxTerminalChangeCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxGenericChangeCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxGraphCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxGraphViewCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxStylesheetCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxDefaultKeyHandlerCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxDefaultToolbarCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxDefaultPopupMenuCodec.js');
    mxClient.include(mxClient.basePath + '/js/io/mxEditorCodec.js');
// PREPROCESSOR-REMOVE-START
}
// PREPROCESSOR-REMOVE-END
