package nolife;

import java.io.Serializable;

public class MessageOfferResult extends MessageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public String usrNic;
    public String usrToOffer;
    public boolean accepted;

    public MessageOfferResult( String errorMessage ) { //Error

        super( Protocol.CMD_OFFER, errorMessage );
        accepted = false;
    }

    public MessageOfferResult(String usrNic,String usrToOffer,boolean accepted) { // No errors

        super( Protocol.CMD_OFFER );
        this.usrNic=usrNic;
        this.usrToOffer=usrToOffer;
        this.accepted = accepted;
    }
}