package mc.gouv.xaf.servlet;

/*****************************************************************************
*
* "Open source" kit for Monetico Paiement (TM)
*
* File "MoneticoPaiement_Ept.java":
*
* Author   : Euro-Information/e-Commerce
* Version  : 4.0
* Date      : 06/06/2014
*
* Copyright: (c) 2014 Euro-Information. All rights reserved.
* License  : see attached document "License.txt".
*
*****************************************************************************/

/*===========================================================================*
 *                              Imported modules
 *===========================================================================*/

final public class MoneticoPaiement_Ept {

	public String sVersion;
	public String sNumero;
	public String sUrlPaiement;
	public String sCodeSociete;
	public String sLangue;
	public String sUrlOk;
	public String sUrlKo;
	
	private String _sCle;
	
	public MoneticoPaiement_Ept (String sLang) throws Exception {
		
		this.sVersion 		= MoneticoPaiement_Config.MONETICOPAIEMENT_VERSION;
		this._sCle 		    = MoneticoPaiement_Config.MONETICOPAIEMENT_KEY;
		this.sNumero 		= MoneticoPaiement_Config.MONETICOPAIEMENT_EPTNUMBER;
		this.sUrlPaiement 	= MoneticoPaiement_Config.MONETICOPAIEMENT_URLSERVER + MoneticoPaiement_Config.MONETICOPAIEMENT_URLPAYMENT;
		this.sCodeSociete  = MoneticoPaiement_Config.MONETICOPAIEMENT_COMPANYCODE;
		this.sLangue 		= (sLang != null) ? sLang : "FR";
		this.sUrlOk 		    = MoneticoPaiement_Config.MONETICOPAIEMENT_URLOK;
		this.sUrlKo 		    = MoneticoPaiement_Config.MONETICOPAIEMENT_URLKO;
	}
	
	public MoneticoPaiement_Ept () throws Exception {
		
		this.sVersion 		= MoneticoPaiement_Config.MONETICOPAIEMENT_VERSION;
		this._sCle 		    = MoneticoPaiement_Config.MONETICOPAIEMENT_KEY;
		this.sNumero 		= MoneticoPaiement_Config.MONETICOPAIEMENT_EPTNUMBER;
		this.sUrlPaiement 	= MoneticoPaiement_Config.MONETICOPAIEMENT_URLSERVER + MoneticoPaiement_Config.MONETICOPAIEMENT_URLPAYMENT;
		this.sCodeSociete  = MoneticoPaiement_Config.MONETICOPAIEMENT_COMPANYCODE;
		this.sLangue 		= "FR";
		this.sUrlOk 		    = MoneticoPaiement_Config.MONETICOPAIEMENT_URLOK;
		this.sUrlKo 		    = MoneticoPaiement_Config.MONETICOPAIEMENT_URLKO;
	}
		
	public String getCle() {
		
		return this._sCle;
	}
}

// End of abstract class


/*===========================================================================*/
