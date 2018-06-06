package de.hpi.unicorn.adapter.GoodsTag;

import de.hpi.unicorn.adapter.AdapterJob;
import de.hpi.unicorn.adapter.EventAdapter;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import java.net.URI;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import java.util.Date;

/*
Example: https://stackoverflow.com/questions/26452903/javax-websocket-client-simple-example
 */

public class GoodsTagAdapter extends EventAdapter {

    public GoodsTagAdapter(String name) {
        super(name);

        // TODO: configure tcp client/server
        // TODO: check, whether authentication/encryption is needed (SSL?)
    }

    @Override
    public void start(long interval) {
        // overrides the base implementation in order to shorten the trigger interval
        // the base implementation assumed the interval in seconds, we want the interval in milliseconds
        try {
            final org.quartz.Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            final JobDetail jd = new JobDetail(this.name, Scheduler.DEFAULT_GROUP, AdapterJob.class);
            final SimpleTrigger simpleTrigger = new SimpleTrigger(this.name, Scheduler.DEFAULT_GROUP, new Date(), null, SimpleTrigger.REPEAT_INDEFINITELY, interval);

            scheduler.scheduleJob(jd, simpleTrigger);
            scheduler.start();
        } catch (final SchedulerException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void trigger() {
        // TODO: pull pending data from the tcp connection
    }

    @Override
    public boolean stop() {
        // TODO: close tcp connection/shut down tcp server
        return super.stop();
    }
}
