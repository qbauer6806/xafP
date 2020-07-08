// JavaScript Document
/*	
 *	jQuery windowResizeFix
 *	
 *	Copyright (c) 2012 Fred Heusschen
 *	www.frebsite.nl
 *
 *	Dual licensed under the MIT and GPL licenses.
 *	http://en.wikipedia.org/wiki/MIT_License
 *	http://en.wikipedia.org/wiki/GNU_General_Public_License
 */


(function( $ )
{

	if ( document.windowResizeFixFired )
	{
		return;
	}
	document.windowResizeFixFired = true;

	var $window = $(window),
		_wWidth = $window.width(),
		_wHeight = $window.height();

	$window.bind(
		'resize',
		function( e )
		{
			var _nWidth = $window.width(),
				_nHeight= $window.height();

			if ( _wWidth == _nWidth && _wHeight == _nHeight )
			{
				e.preventDefault();
				e.stopImmediatePropagation();
				return;
			}
			_wWidth = _nWidth;
			_wHeight = _nHeight;
		}
	);

})( jQuery );
/*Config parameters
lang : 'fr' or 'en'
gotTooltips : true or false
*/
if (typeof gotTooltips == 'undefined'){
	gotTooltips = true;
}
/*
	language files for JS...
	set lang BEFORE including this file into page.
	French is default language.
*/
if (typeof jsLang == 'undefined'){
	jsLang = "fr";
}
/*
Debug Mode
*/
if (typeof jsDebug == 'undefined'){
	jsDebug = false;
}


//Language variables handling
gouvRegional = new Array('fr','en');
gouvRegional['fr']={
	closepopin : "Fermer la pop-in"
}
gouvRegional['en']={
	closepopin : "Close the pop-in"
}

if(gouvRegional['fr'].length != gouvRegional['en'].length){
	$.logThis("Warning : translation missing");
}

/*
Returns a localized text depending on current language setting
*/
function getProp(key){
	return window.gouvRegional[jsLang][key];	
}

/*
	Allows to add more translation texts on the fly.
		key : identifier (ex : 'closepopin')
		text_fr : french translsation (ex : "Fermer la pop-in");
		text_en : english translation (ex : "Close the pop-in");
*/

function setProps(key,fr_text,en_text){
	gouvRegional['fr'][key] = fr_text;
	gouvRegional['en'][key] = en_text;
	
}

jQuery.logThis = function(text){
	if(jsDebug == true){
		if((typeof window["console"] !== 'undefined')){
			console.log(text);	
		}else{
			$("body").append("<div>"+text+"</div>");	
		}
	}
}

/* makes Tooltip using jquery + jquerytoolbox

Cette fonction génère un tooltip au survol d'un pictogramme dont
le contenu est un calque masqué plus bas.
paramètres 
sell : identifiant jquery de l'élément qui affiche le tooltip
contenu : identifiant jquery de l'élément à afficher dans le tooltip
opt_pos : "top left" ou "top right". indique l'emplecement du tooltip par rapport à l'élément survolé
*/
/*function makeTooltip(sel1,contenu,opt_pos){
	$.logThis(sel1 + "à afficher ");
	if (typeof opt_pos === 'undefined') { opt_pos = "top left"; };
	switch(opt_pos){
		case "top left":
			val_offset = [-10,16];
			break;
		case "top right":
		default:
			val_offset = [-5,5];
			break;
	}
 	$(sel1).tooltip({
	  relative:true,
	  position: opt_pos,
	  offset: val_offset,
	  effect: "fade",
	  opacity: 1,
	  content: "WTF"
    });
}*/

/*

Creates a tooltip on an element using the html code targeted

sel1 : jquery selector of the element that triggers the tooltip
contenu : jquery selector of the content to be used in the tooltip
opt_pos : *deprecated*

*/
function makeTooltip(sel1,contenu,opt_pos){
	$(sel1).parent().tooltip({items:"a",content:function(callback){
				callback($(contenu).html());	
	}});
}


/* alters font size (accessibility) */
function resizeText(multiplier) {
  if (document.body.style.fontSize == "") {
    document.body.style.fontSize = "0.9em";
  }
  var fSize = parseFloat(document.body.style.fontSize) + (multiplier * 0.2);
  if(fSize>2.5){
	  fSize=2.5
  }
  if(fSize<0.5){
	fSize=0.6;
  }
  document.body.style.fontSize = fSize + "em";
}

/* creates popins from args content 
pContent : Contenu de la popin (HTML DOM)
*deprecated* pID : ID CSS de la popin 
JSON params supported :
	id : ID CSS de la popin
	width : largeur en px de la popin
	height : hauteur en px de la popin
	title : Titre de la popin (Texte)
	modal : false or true
	draggable : false or true
	left : position par rapport au coin supérieur gauche de l'écran
	top : position par rapport au coin supérieur droit de l'écran
	target : target de la popin (default : body) (using jQuery selectors)
	(a venir) tgtMethod : methode d'insertion de la popin. valeurs : append (default), insertBefore
	
remarque : Pour fermer la pop-in courante au moyen d'un bouton à l'intérieur de cette dernière, il suffit de doter le bouton de la classe "close_popin"
*/
function createPopIn(pContent,pParams){
	//myWidth = pWidth || "400";
	
	var P = prepPopInParams(pParams);
	//window.alert(pContent + " / width:" + myWidth);
	//if fader is not here
	if($("#fade").length == 0){
		$("body").append('<div id="fade"></div>');
	}
	removePopIn(P.id);
	
	//prepare popin
	var popinCode="";
	popinCode+='<div id="'+P.id+'" class="popin">';
	popinCode+='<div class="popin_header">';
	$("#fade").unbind();
	if(P.modal == false){
		$("#fade").click(function(){popInClose(P.id)});
	}
	if(P.modal == false){
		popinCode+='<a href="#" class="popin_dl_close" onclick="popInClose(\''+P.id+'\');" title="'+getProp("closepopin")+'"></a>';
	}
	if(P.title != ""){
		popinCode+='<div class="popin_title">'+P.title+"</div></div>";
	}else{
		popinCode+='<div class="popin_title">&nbsp;</div></div>';
	}
	
	//insertion du contenu
	if(pContent.substring(0,1) !="#"){
		// cas code source
		popinCode += '<div class="inner_white">';
		popinCode += pContent;
		popinCode += "</div></div>";
		$("body").append(popinCode);
		P.ptype = "removable";
	}else{
		//cas #id
		popinCode += '<div class="inner_white"></div></div>';
		
		$(popinCode).insertBefore(pContent);
		
		//$("#"+P.id).data("P",P);
		var myDOM = $(pContent).detach();
		$("#"+P.id+ " .inner_white").append(myDOM);
		P.ptype = "restore";
		P.origContent = pContent;
		$(pContent).css("display","block");
	}

	
	$("#"+P.id+" .close_popin").click(function(){popInClose(P.id)});
	//$("#").P = P;
	$("#"+P.id).data("P",P);
	$('#' + P.id).css({
		'z-index':9998,
		'position':"fixed",
		'display':"none"
	});
	
	
	setTimeout(function(){resizePopIn(P.id)},10);
	
	//placement et affichage
	if(P.drag == false){
		if($("#fade").css("display") == "none"){
			$("#fade").fadeIn();
		}
	}

	$("#"+P.id).fadeIn();
	
	if(P.drag == true){
		$("#" + P.id).draggable();	
	}
	
	//$.logThis($("#"+P.id).data("P.title"));
	//activation des tooltips eventuels de la popin
	createTooltips();
	
	//Prise en charge de resize
	//$( window ).resize(P.id, resizePopIn);
	$(window).on('resize.GouvMCPopin'+P.id, function () {
  		resizePopIn(P.id);
	});
}

function prepPopInParams(pParams){
	var P = new Object();
	if((typeof pParams !== 'undefined')&&(pParams != null)){
		(typeof pParams.left !== 'undefined') ? P.left = pParams.left : P.left="auto";
		(typeof pParams.top !== 'undefined') ? P.top = pParams.top : P.top="auto";
		(typeof pParams.width !== 'undefined') ? P.width = pParams.width : P.width="auto";
		(typeof pParams.height !== 'undefined') ? P.height = pParams.height : P.height="auto";
		(typeof pParams.modal !== 'undefined') ? P.modal = pParams.modal: P.modal=false;
		(typeof pParams.title !== 'undefined') ? P.title = pParams.title: P.title="";
		(typeof pParams.draggable !== 'undefined') ? P.drag = pParams.draggable: P.drag=false;
		(typeof pParams.id !== 'undefined') ? P.id = pParams.id : P.id = "noId";
		(typeof pParams.target !== 'undefined') ? P.target = pParams.target : P.target = "body";
		(typeof pParams.tgtMethod !== 'undefined') ? P.tgtMethod = pParams.tgtMethod: P.tgtMethod = "append";
	}else{
		P.top="auto";
		P.left="auto";
		P.width="auto";
		P.height="auto";
		P.modal=false;
		P.title="";
		P.drag = false;
		P.id = "noId";
	}
	return P;
}


/*

Function that redraws popin according to its parameters / content

pId : id attribute of the popin

*/
function resizePopIn(pId){
	var popin = $("#"+pId);
	var P = popin.data("P");
    $.logThis("before calc :"+P.width+" x "+P.height);
	//calcul de taille de la pop-in	
	popin.css({
		'top' : 0,
		'left' : 0
	});
	
	$('#' + P.id + " .inner_white").css({
		'width':P.width,
		'height':P.height
	});	
	
	calcwidth = 0;
	calcheight = 0;
	
	//$.logThis("Content Dims :"+$(window).width()+" x "+$(window).height());
	if(($('#' + P.id + " .inner_white").width() >= 950)&&(P.width=="auto")){
		$.logThis("recalculating width...");
		$('#' + P.id).css({width:"950px"});
		$('#' + P.id +" .inner_white").css({width:"930px"});
	}else{
		$('#' + P.id).css({width:($('#' + P.id +" .inner_white").width()+20) +"px"});
	}
	
	if(($('#' + P.id).height() >= $(window).height()*80/100)&&(P.height=="auto")){
		$('#' + P.id +" .inner_white").height($(window).height()*80/100 +"px");
		$.logThis("recalculating height...");
	}else{
	}
	$('#' + P.id).height( $('#' + P.id).height + $('#' + P.id +" .inner_white").height() );
	//$('#' + P.id).css({height:($('#' + P.id +" .inner_white").height()+40) +"px"});
	
	//position de la pop-in
	if((typeof P.left === 'undefined')||(P.left == "auto")){
		var popLeft = ($(window).width()-$("#"+P.id).outerWidth())/2;
		if(popLeft <=0) popLeft = 0;
	}else{
		popLeft = P.left;	
	}
	
	if((typeof P.top === 'undefined')||(P.top == "auto")){
		var popTop = ($(window).height()-$("#"+P.id).outerHeight())/2;
		if(popTop <= 0) popTop = 0;
	}else{
		popTop = P.top;
	}

	popin.css({
		'top' : popTop,
		'left' : popLeft
	});
	
	$.logThis("popin (" +popTop + "," + popLeft +"). Taille " + $("#"+P.id + " .inner_white").width() + " ("+P.width+") x "+$("#"+P.id + " .inner_white").height()+" ("+P.height+")");
}


/*

Called by close button within popins. 
Can be called manually

pId : id attribute of the popin

*/
function popInClose(pId){
	$("#"+pId).fadeOut(500,removePopIn(pId));
	$("#fade").fadeOut();	
}

/*

Removes a popin, depending its calling parameters (c.f createPopin)

pId : id attribute of the popin

*/
function removePopIn(pId){
	if(typeof $("#"+pId).data("P") !== 'undefined'){
		if ($("#"+pId).data("P").ptype == "removable"){
			$.logThis("Kill da popin");
			$("#"+pId).remove();
		}else{
			$.logThis("return content in place");
			var Dom = $($("#"+pId).data("P").origContent).detach();
			$("#"+pId).before(Dom);
			$("#"+pId).remove();
			Dom.css("display","none");
		}	
	}
	$(window).off('resize.GouvMCPopin'+pId);
}

/*

calls distant content and opens it in a pop-in

pUrl : Url of the content
params : parameters object used in 

*/
function ajaxPopIn(pUrl,params){
	$.ajax({
		url:pUrl,
		context:params,
		success:function(data){
			createPopIn(data,params);
		}
	});
}


function createTooltips(){
	if(gotTooltips == true){
		$("*[title]").not(".no-tooltip").tooltip({
			track:true,
			position:{
				my:"left top+20"	
			}
		});	
		$(document).tooltip({items:".ttp2",content:function(callback){
			madest = $(this).attr("href");
			if(madest.substring(0,1) == "#"){
				callback($(madest).html());	
			}
		}});	
	}
}

$(document).ready(function(){
	//force l'ouverture des liens de classe "ajaxpopin" en pop-in
	$(".ajaxpopin").click(function(){
		if($(this).attr("href") != "#"){
			$(this).attr("dest",$(this).attr("href"));
			$(this).attr("href","#");
		}
		$.logThis($(this).attr("rel"));
		jsonParams = $.parseJSON($(this).attr("rel"));
		ajaxPopIn($(this).attr("dest"),jsonParams);
	});
	createTooltips();
});
