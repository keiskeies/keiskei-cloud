(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["pages-cloud-video-index"],{"06ef":function(t,e,o){"use strict";var a=o("4ea4");o("4160"),o("fb6a"),o("a434"),o("e25e"),o("159b"),Object.defineProperty(e,"__esModule",{value:!0}),e.default=void 0;var i=a(o("5530")),r=o("2f62"),s={computed:(0,i.default)({},(0,r.mapState)(["vuex_bar_color"])),data:function(){return{loadStatus:"loadmore",flowList:[],videoUploadItem:{progress:0},operationItem:{},operationShow:!1,operationList:[{text:"删除",color:"red",fontSize:28}],scrollTop:0,total:10}},onLoad:function(){this.addRandomData()},onShow:function(){},onPageScroll:function(t){this.scrollTop=t.scrollTop},onReachBottom:function(){this.flowList.length<this.total&&this.addRandomData()},methods:{addRandomData:function(){var t=this;this.flowList.length>=this.total?this.loadStatus="nomore":(this.loadStatus="loading",this.$u.get("/api/file/video/list?offset="+this.flowList.length).then((function(e){var o=e.total,a=e.data;t.total=o,a.forEach((function(e){e.id=t.$u.guid(),t.flowList.push(e)})),t.flowList.length>=t.total?t.loadStatus="nomore":t.loadStatus="loadmore"})).catch((function(e){t.loadStatus="loadmore"})))},reloadVideo:function(){var t=this;this.$u.get("/api/file/video/sort").then((function(){t.flowList=[],t.$refs.uWaterfall.clear(),t.addRandomData()}))},showDetal:function(t){this.$u.route({url:"pages/cloud/video/detail",params:{videoName:t.name}})},oprate:function(t){this.operationItem=t,this.operationShow=!0},operationClick:function(t){0===t&&this.delete(this.operationItem)},chooseVideo:function(){var t=this;uni.chooseVideo({success:function(e){console.log(e),t.uploadVideo(e.tempFilePath)}})},progressColor:function(t){var e=230-2*t,o=150+parseInt(t/2),a=10+parseInt(t);return"rgb("+e+", "+o+", "+a+")"},uploadVideo:function(t){var e=this,o=function(){e.videoUploadItem.progress=0;var o=uni.uploadFile({url:e.$u.http.config.baseUrl+"/api/file/video/upload",filePath:t,name:"file",success:function(t){var o=JSON.parse(t.data);o.id=e.$u.guid(),e.flowList.splice(0,0,o),e.$refs.uTips.show({title:"上传成功",type:"success",duration:"2300"}),e.videoUploadItem.progress=0},fail:function(t){e.$refs.uTips.show({title:"上传失败",type:"error",duration:"2300"}),e.videoUploadItem.progress=0}});o.onProgressUpdate((function(t){console.log(t),e.videoUploadItem.progress=t.progress}))};o()},uploadVideo1:function(t){var e=this;e.videoUploadItem.progress=10;var o=t.size,a=5242880,i=Math.ceil(o/a),r=0,s=0,n=function(){e.videoUploadItem.progress=99,e.$u.post("/api/file/video/mergingPart",{fileName:t.name}).then((function(t){t.id=e.$u.guid(),e.flowList.unshift(t),e.videoUploadItem.progress=0,e.$refs.uTips.show({title:"上传成功",type:"error",duration:"2300"})})).catch((function(t){console.error(t),e.$refs.uTips.show({title:"上传失败",type:"error",duration:"2300"})}))},l=function(t,e,a){var i=e*a,r=Math.min(o,i+a),s=t.slice(i,r);return{start:i,end:r,chunk:s}},d=function d(u){var c=l(t,u,a),f=c.chunk,p=new FileReader;p.readAsDataURL(f),p.addEventListener("load",(function(a){var l=a.target.result;l=l.substring(37),uni.request({url:e.$u.http.config.baseUrl+"/api/file/video/uploadBlobPart",method:"POST",data:{fileName:t.name,fileSize:o,partCount:i,index:u,blobBase64:l},success:function(){s++,r=Math.ceil(s/i*98),e.videoUploadItem.progress=r,s>=i?n():d(u+1)},fail:function(t){console.error(t),e.videoUploadItem.progress=0,e.$refs.uTips.show({title:"上传失败",type:"error",duration:"2300"})}})}),!1)};d(0)},delete:function(t){var e=this;this.$u.delete("/api/file/video/delete/"+t.name).then((function(o){for(var a=e.flowList.length,i=0;i<a;i++){var r=e.flowList[i];if(r.id==t.id){e.flowList.splice(i,1);break}}e.videoSwipeCurrent>e.flowList.length-3&&e.addRandomData(),e.$refs.uWaterfall.remove(t.id),e.total--}))}}};e.default=s},"0edc":function(t,e,o){"use strict";o.r(e);var a=o("06ef"),i=o.n(a);for(var r in a)"default"!==r&&function(t){o.d(e,t,(function(){return a[t]}))}(r);e["default"]=i.a},"2d68":function(t,e,o){var a=o("6846");"string"===typeof a&&(a=[[t.i,a,""]]),a.locals&&(t.exports=a.locals);var i=o("4f06").default;i("34e0ea34",a,!0,{sourceMap:!1,shadowMode:!1})},3634:function(t,e,o){"use strict";o.r(e);var a=o("3744"),i=o("0edc");for(var r in i)"default"!==r&&function(t){o.d(e,t,(function(){return i[t]}))}(r);o("e1f7");var s,n=o("f0c5"),l=Object(n["a"])(i["default"],a["b"],a["c"],!1,null,"63114ed3",null,!1,a["a"],s);e["default"]=l.exports},3744:function(t,e,o){"use strict";o.d(e,"b",(function(){return i})),o.d(e,"c",(function(){return r})),o.d(e,"a",(function(){return a}));var a={uNavbar:o("938f").default,uIcon:o("ec4d").default,uCircleProgress:o("e4e1").default,uWaterfall:o("9351").default,uLazyLoad:o("7823").default,uLoadmore:o("1a90").default,uBackTop:o("5f1a").default,uTopTips:o("eea5").default,uActionSheet:o("9d81").default},i=function(){var t=this,e=t.$createElement,o=t._self._c||e;return o("v-uni-view",{class:t.vuex_theme},[o("u-navbar",{staticClass:"video-detail-content-title",attrs:{title:"视频","is-back":!1,"title-bold":!0,background:{background:t.vuex_bar_color},"border-bottom":!1}},[o("v-uni-view",{staticClass:"slot-wrap",attrs:{slot:"right"},slot:"right"},[o("u-icon",{staticClass:"slot-wrap-right",attrs:{name:"reload",size:"40"},on:{click:function(e){arguments[0]=e=t.$handleEvent(e),t.reloadVideo.apply(void 0,arguments)}}}),t.videoUploadItem.progress?o("u-circle-progress",{staticClass:"pre-box-item-progress slot-wrap-right",attrs:{width:40,"border-width":"3",percent:t.videoUploadItem.progress,"bg-color":"none",round:!0,"active-color":t.progressColor(t.videoUploadItem.progress)}}):o("u-icon",{staticClass:"slot-wrap-right",attrs:{name:"plus",size:"40"},on:{click:function(e){arguments[0]=e=t.$handleEvent(e),t.chooseVideo.apply(void 0,arguments)}}})],1)],1),o("u-waterfall",{ref:"uWaterfall",scopedSlots:t._u([{key:"left",fn:function(e){var a=e.leftList;return t._l(a,(function(e,a){return o("v-uni-view",{key:a,staticClass:"video-warter color-gradual",on:{click:function(o){arguments[0]=o=t.$handleEvent(o),t.showDetal(e)},longpress:function(o){arguments[0]=o=t.$handleEvent(o),t.oprate(e)}}},[o("u-lazy-load",{staticClass:"color-gradual-same",attrs:{threshold:-10,"border-radius":"10",image:t.$u.http.config.baseUrl+encodeURI(e.url)+"?x-oss-process=video/snapshot,t_5000,m_jpg",index:a}}),o("v-uni-view",{staticClass:"video-title"},[t._v(t._s(e.name))])],1)}))}},{key:"right",fn:function(e){var a=e.rightList;return t._l(a,(function(e,a){return o("v-uni-view",{key:a,staticClass:"video-warter color-gradual",on:{click:function(o){arguments[0]=o=t.$handleEvent(o),t.showDetal(e)},longpress:function(o){arguments[0]=o=t.$handleEvent(o),t.oprate(e)}}},[o("u-lazy-load",{staticClass:"color-gradual-same",attrs:{threshold:-10,"border-radius":"10",image:t.$u.http.config.baseUrl+encodeURI(e.url)+"?x-oss-process=video/snapshot,t_5000,m_jpg",index:a}}),o("v-uni-view",{staticClass:"video-title"},[t._v(t._s(e.name))])],1)}))}}]),model:{value:t.flowList,callback:function(e){t.flowList=e},expression:"flowList"}}),o("u-loadmore",{attrs:{status:t.loadStatus},on:{loadmore:function(e){arguments[0]=e=t.$handleEvent(e),t.addRandomData.apply(void 0,arguments)}}}),o("u-back-top",{staticClass:"color-gradual",attrs:{"scroll-top":t.scrollTop,top:"1"}}),o("u-top-tips",{ref:"uTips"}),o("u-action-sheet",{staticClass:"color-ico-gradual",attrs:{list:t.operationList,"cancel-btn":!1},on:{click:function(e){arguments[0]=e=t.$handleEvent(e),t.operationClick.apply(void 0,arguments)}},model:{value:t.operationShow,callback:function(e){t.operationShow=e},expression:"operationShow"}})],1)},r=[]},6846:function(t,e,o){var a=o("24fb");e=a(!1),e.push([t.i,'@charset "UTF-8";\r\n/**\r\n * 下方引入的为uView UI的集成样式文件，为scss预处理器，其中包含了一些"u-"开头的自定义变量\r\n * 使用的时候，请将下面的一行复制到您的uniapp项目根目录的uni.scss中即可\r\n * uView自定义的css类名和scss变量，均以"u-"开头，不会造成冲突，请放心使用 \r\n */.video-warter[data-v-63114ed3]{border-radius:8px;margin:5px;padding:8px;position:relative}.slot-wrap-right[data-v-63114ed3]{margin:%?2?% %?20?%}.u-close[data-v-63114ed3]{position:absolute;top:%?32?%;right:%?32?%}.video-image[data-v-63114ed3]{width:100%;border-radius:4px}.video-title[data-v-63114ed3]{font-size:%?20?%;margin-top:5px;color:#909399;word-break:break-all}.video-dialog[data-v-63114ed3]{width:100%;height:100%}.upload-btn[data-v-63114ed3]{position:absolute;right:0;bottom:0}',""]),t.exports=e},e1f7:function(t,e,o){"use strict";var a=o("2d68"),i=o.n(a);i.a}}]);