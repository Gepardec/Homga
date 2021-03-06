package com.gepardec.hogarama.domain.watering;

import javax.inject.Inject;

import com.gepardec.hogarama.domain.unitmanagement.cache.ActorCache;
import com.gepardec.hogarama.domain.unitmanagement.cache.SensorCache;
import com.gepardec.hogarama.domain.unitmanagement.entity.Actor;
import com.gepardec.hogarama.domain.unitmanagement.entity.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gepardec.hogarama.domain.metrics.Metrics;
import com.gepardec.hogarama.domain.sensor.SensorDataDAO;
import com.gepardec.hogarama.domain.sensor.SensorData;

import java.util.Optional;

public class WateringService {

	public enum Config{

		DEFAULT(1, 0.2, 5);
		
		protected int measureInterval;
		protected double lowWater;
		protected int waterDuration;
		
		Config( int measureInterval, double lowWater, int waterDuration) {
			this.waterDuration = waterDuration;
			this.measureInterval = measureInterval;
			this.lowWater = lowWater;
		}
	}

	@Inject
	public SensorDataDAO sensorDataDAO;

	@Inject
	public WateringConfigDAO configDao;

	@Inject
	public ActorControlService actorSvc;

	@Inject
	public WateringStrategy watering;

	@Inject
	private ActorCache actorCache;

	@Inject
	private SensorCache sensorCache;

    private static final Logger log = LoggerFactory.getLogger(WateringService.class);

	public WateringService() {
	}

	protected WateringService(SensorDataDAO sensorDataDAO, ActorControlService actorSvc, WateringStrategy watering, WateringConfigDAO configDao, ActorCache actorCache, SensorCache sensorCache) {
		this.sensorDataDAO = sensorDataDAO;
		this.actorSvc = actorSvc;
		this.watering = watering;
		this.configDao = configDao;
		this.actorCache = actorCache;
		this.sensorCache = sensorCache;
	}

    private void invokeActorIfNeeded(WateringConfigData config, int dur, String location) {
        if (dur > 0) {

			Optional<Actor> optionalActor = actorCache.getByDeviceId(config.getActorName());
			String actorName = optionalActor.isPresent() ? optionalActor.get().getName() : config.getActorName();
			Optional<Sensor> optionalSensor = sensorCache.getByDeviceId(config.getSensorName());
			String sensorName = optionalSensor.isPresent() ? optionalSensor.get().getName() : config.getSensorName();

			Metrics.wateringEventsFired.labels(sensorName, actorName).inc();
			Metrics.actorValues.labels(
					actorName,
					sensorName,
					location
					).set(dur);

        	actorSvc.sendActorMessage(location, config.getActorName(), dur);
        }
        else {
            log.debug("Don't water {}", config.getActorName());
        }
    }

	private WateringConfigData getConfig(String sensorName) {
		WateringConfigData wconfig = configDao.getBySensorName(sensorName);
		if ( null != wconfig ) {
			return wconfig;
		}
		
		wconfig = new WateringConfigData(sensorName, sensorName, Config.DEFAULT.measureInterval,
				Config.DEFAULT.lowWater, Config.DEFAULT.waterDuration);
		configDao.save(wconfig);
		return wconfig;
	}

    public void water(SensorData sensorData) {
        WateringConfigData config = getConfig(sensorData.getSensorName());
        invokeActorIfNeeded(config, watering.computeWateringDuration(config, sensorData.getValue()), sensorData.getLocation());
    }

}
