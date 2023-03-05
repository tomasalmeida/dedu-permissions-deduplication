package com.tomasalmeida.dedu.permission.acls;

import com.tomasalmeida.dedu.Configuration;
import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.BindingDeduplicator;
import com.tomasalmeida.dedu.permission.acls.modifiers.InexistentTopicBindingModifier;


public class AclBindingDeduplicator extends BindingDeduplicator {

    public AclBindingDeduplicator(final KafkaAdminClient adminClient, final Configuration configuration) {
        super(adminClient, configuration);
        this.addRule(new InexistentTopicBindingModifier(adminClient));
    }
}
