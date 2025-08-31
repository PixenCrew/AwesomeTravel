package renewal.awesome_travel.air.domain;


import lombok.Getter;
import renewal.awesome_travel.air.entity.SeatClass;
import renewal.awesome_travel.air.utiles.SeatClassType;

@Getter
public class RoundTripSearchResult {
    private SeatClass outbound;
    private SeatClass inbound;
    private int totalPrice;

    public RoundTripSearchResult(SeatClass outbound, SeatClass inbound, SeatClassType classType) {
        this.outbound = outbound;
        this.inbound = inbound;
        this.totalPrice = outbound.getPrice() + inbound.getPrice();
    }

    public SeatClass getOutbound() { return outbound; }
    public SeatClass getInbound() { return inbound; }

    public String getOutboundDepartTime() { return outbound.getAir().getDepart_time(); }
    public String getInboundDepartTime() { return inbound.getAir().getDepart_time(); }

    public int getTotalPrice() { return totalPrice; }
}

