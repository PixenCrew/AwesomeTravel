package renewal.awesome_travel.air.domain;


import lombok.Getter;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.utiles.SeatClassType;

@Getter
public class RoundTripSearchResult {
    private Air outbound;
    private Air inbound;
    private int totalPrice;

    public RoundTripSearchResult(Air outbound, Air inbound, SeatClassType classType) {
        this.outbound = outbound;
        this.inbound = inbound;
        this.totalPrice = outbound.getPrice(classType) + inbound.getPrice(classType);
    }

    public Air getOutbound() { return outbound; }
    public Air getInbound() { return inbound; }

    public String getOutboundDepartTime() { return outbound.getDepart_time(); }
    public String getInboundDepartTime() { return inbound.getDepart_time(); }

    public int getTotalPrice() { return totalPrice; }
}

