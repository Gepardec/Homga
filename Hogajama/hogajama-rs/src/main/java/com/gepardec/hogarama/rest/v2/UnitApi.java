package com.gepardec.hogarama.rest.v2;

import com.gepardec.hogarama.domain.entity.*;
import com.gepardec.hogarama.rest.SensorData;
import com.gepardec.hogarama.rest.interceptor.DetermineOwner;
import com.gepardec.hogarama.rest.translator.UnitDtoTranslator;
import com.gepardec.hogarama.rest.v2.dto.UnitDto;
import com.gepardec.hogarama.service.OwnerStore;
import com.gepardec.hogarama.service.UnitService;
import com.gepardec.hogarama.service.dao.ActorDao;
import com.gepardec.hogarama.service.dao.OwnerDao;
import com.gepardec.hogarama.service.dao.SensorDataDAO;
import com.google.common.base.Strings;
import org.apache.http.HttpStatus;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@DetermineOwner
@Path("v2/unit")
public class UnitApi implements ApiBase<UnitDto> {

    @Inject
    private UnitService service;
    @Inject
    private UnitDtoTranslator translator;
    @Inject
    private OwnerDao ownerDao;
    @Inject
    private ActorDao actorDao;
    @Inject
    private SensorDataDAO sensorDataDAO;
    @Inject
    private OwnerStore store;

    @Override
    public Response getAll(SecurityContext securityContext) {
        List<UnitDto> units = translator.toDtoList(service.getAllUnits());
        return new BaseResponse<>(units, HttpStatus.SC_OK).createRestResponse();
    }

    @Override
    public Response getForOwner(SecurityContext securityContext) {
        List<UnitDto> dtoList = translator.toDtoList(service.getUnitsForOwner());
        return new BaseResponse<>(dtoList, HttpStatus.SC_OK).createRestResponse();
    }

    @Override
    @Transactional
    public Response create(SecurityContext securityContext, UnitDto unitDto) {
        Unit unit = translator.fromDto(unitDto);
        service.createUnit(unit);

        return new BaseResponse<>(HttpStatus.SC_CREATED).createRestResponse();
    }

    @Override
    @Transactional
    public Response update(String id, SecurityContext securityContext, UnitDto unitDto) {
        Unit sensor = translator.fromDto(unitDto);
        if (id == null || !id.equals(unitDto.getId().toString())) {
            return new BaseResponse<>(HttpStatus.SC_BAD_REQUEST).createRestResponse();
        } else {
            service.updateUnit(sensor);
        }

        return new BaseResponse<>(HttpStatus.SC_OK).createRestResponse();
    }

    @Override
    @Transactional
    public Response delete(String id, SecurityContext securityContext) {
        if (id == null) {
            return new BaseResponse<>(HttpStatus.SC_BAD_REQUEST).createRestResponse();
        } else {
            Long idNum;
            try {
                idNum = Long.parseLong(id);
            } catch (NumberFormatException e) {
                return new BaseResponse<>(HttpStatus.SC_BAD_REQUEST).createRestResponse();
            }

            service.deleteUnit(idNum);
        }

        return new BaseResponse<>(HttpStatus.SC_OK).createRestResponse();
    }

    @POST
    @Path("refreshHardwareCode")
    @Transactional
    public Response refreshHardwareCode(SecurityContext securityContext){
        Owner ow = store.getOwner();

        byte[] array = new byte[16];
        new Random().nextBytes(array);
        String randomCode = new String(array, StandardCharsets.UTF_8);

        ow.setCurrentHardwareRegisterCode(randomCode);

        ownerDao.update(ow);
        return new BaseResponse<>(HttpStatus.SC_OK).createRestResponse();
    }

    @POST
    @Path("addHardware")
    @Transactional
    public Response addNewHardwareToDefaultUnit(String oneTimeUseCode, String macAddress, boolean isActor, SensorType type){
        if(Strings.isNullOrEmpty(oneTimeUseCode) || Strings.isNullOrEmpty(macAddress)) {
            return new BaseResponse<>(HttpStatus.SC_BAD_REQUEST).createRestResponse();
        }

        String code = oneTimeUseCode.substring(0, 15);
        long userId = Long.parseLong(oneTimeUseCode.substring(16));
        Owner owner = ownerDao.getById(userId).orElse(null);

        if(owner == null || !owner.getCurrentHardwareRegisterCode().equals(code)) {
            return new BaseResponse<>(HttpStatus.SC_BAD_REQUEST).createRestResponse();
        }

        Unit defaultUnit = owner.getDefaultUnit();

        if(isActor) {
            Actor ac = new Actor();
            ac.setDeviceId(macAddress);
            ac.setName("");
            ac.setUnit(defaultUnit);
            actorDao.save(ac);
        } else {
            Sensor se = new Sensor();
            se.setDeviceId(macAddress);
            se.setName("");
            se.setUnit(defaultUnit);
            se.setSensorType(type);
            sensorDataDAO.save(se);
        }

        return new BaseResponse<>(HttpStatus.SC_OK).createRestResponse();
    }
}
