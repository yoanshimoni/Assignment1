package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {

	public LogisticsService(String name) {
		super(name);
	}

	@Override
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, t -> {
			if (t.isLastTick())
				terminate();
		});

		subscribeEvent(DeliveryEvent.class, d-> {
			Future<Future<DeliveryVehicle>> futureObj = sendEvent(new AcquireVehicleEvent(d.getCustomer().getAddress()));
			if (futureObj != null) {
				Future<DeliveryVehicle> vehicle = futureObj.get();
				if (vehicle != null && vehicle.get() != null) {
					vehicle.get().deliver(d.getCustomer().getAddress(), d.getCustomer().getDistance());
					sendEvent(new ReleaseVehicleEvent(vehicle.get()));
				}
			}
		});
		RunningCounter.getInstance().addRunningThread();
	}

}
