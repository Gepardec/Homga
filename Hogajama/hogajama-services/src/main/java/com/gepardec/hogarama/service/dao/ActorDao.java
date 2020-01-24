package com.gepardec.hogarama.service.dao;

import com.gepardec.hogarama.domain.entity.Actor;
import com.gepardec.hogarama.domain.entity.QActor;
import com.gepardec.hogarama.domain.entity.Sensor;
import com.querydsl.jpa.impl.JPAQuery;

import javax.enterprise.context.Dependent;
import java.util.List;

@Dependent
public class ActorDao extends BaseDao<Actor> {

    @Override
    public Class<Actor> getEntityClass() {
        return Actor.class;
    }

    public List<Actor> getActorsForOwner(Long ownerId) {
        JPAQuery<Sensor> query = new JPAQuery<>(entityManager);
        QActor actor = QActor.actor;
        return query.select(actor).from(actor).where(actor.unit.owner.id.eq(ownerId)).fetch();
    }
}