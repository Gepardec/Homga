package com.gepardec.hogarama.domain.unitmanagement.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.gepardec.hogarama.domain.unitmanagement.entity.EntityNotFoundException;

public abstract class BaseDAO<T extends Serializable> {

    @PersistenceContext
    protected EntityManager entityManager;

    public abstract Class<T> getEntityClass();

    public Optional<T> getById(Long id) {
        return Optional.ofNullable(entityManager.find(getEntityClass(), id));
    }

    public T getByIdNonOpt(Long id) {
        return getById(id).orElseThrow(() -> new EntityNotFoundException(id, getEntityClass()));
    }

    public List<T> findAll() {
        return entityManager.createQuery("from " + getEntityClass().getName())
                .getResultList();
    }

    public void save(T entity) {
        entityManager.persist(entity);
    }

    public void update(T entity) {
        entityManager.merge(entity);
    }

    public void delete(T entity) {
        entityManager.remove(entity);
    }

    public void deleteById(Long id) {
        Optional<T> optionalEntity = getById(id);
        optionalEntity.ifPresent(this::delete);
    }
}
