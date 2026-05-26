package com.vpo.djvoxbox.domain;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class ManagerRepositoryImpl implements ManagerRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Manager returnForWork(String name) {

        Query query = new Query();
        // look for thge manager
        query.addCriteria(Criteria.where("name").is(name));
        // where the manager is active
        query.addCriteria(Criteria.where("active").is(true));
        // and no one is working on it
        query.addCriteria(Criteria.where("workLock").is(null));
        // and it hasn't been updated for 30 seconds
        query.addCriteria(Criteria.where("lastUpdate").lt(new Date(System.currentTimeMillis() - 30000)));

        Update update = new Update();
        // set the worklock to the current time
        update.set("workLock", new Date());

        return mongoTemplate.findAndModify(query, update, Manager.class);
    }

    @Override
    public Manager returnForUsurp(String name) {

        Query query = new Query();
        // look for the manager
        query.addCriteria(Criteria.where("name").is(name));
        // where the manager is active
        query.addCriteria(Criteria.where("active").is(true));
        // and the worklock is older than 3 minutes
        query.addCriteria(Criteria.where("workLock").lt(new Date(System.currentTimeMillis() - 180000L)));
        // and no one else is usurping
        query.addCriteria(Criteria.where("usurping").is(null));

        Update update = new Update();
        // set that you're gonna usurp it
        update.set("usurping", new Date());

        return mongoTemplate.findAndModify(query, update, Manager.class);
    }

    @Override
    public Manager tryAcquireEventLock(String name, long ttlMs) {
        // Atomic claim: the active manager whose workLock is either absent or
        // older than ttlMs is rewritten with workLock=now and returned. If no
        // document matches, findAndModify returns null and the caller knows
        // another instance currently owns the lock.
        Date staleBefore = new Date(System.currentTimeMillis() - ttlMs);

        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        query.addCriteria(Criteria.where("active").is(true));
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("workLock").is(null),
                Criteria.where("workLock").lt(staleBefore)));

        Update update = new Update();
        update.set("workLock", new Date());

        return mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().returnNew(true), Manager.class);
    }

    @Override
    public void releaseEventLock(Manager manager) {
        if (manager == null) {
            return;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(manager.getName()));

        Update update = new Update();
        update.unset("workLock");

        mongoTemplate.findAndModify(query, update, Manager.class);
    }
}
