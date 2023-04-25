# <img src="images/logo.svg" width="24px"> Dedu - Permissions deduplication tool for Apache Kafka®
A heuristic deduplication ACL tool 

[![main-ci](https://github.com/tomasalmeida/dedu-permissions-deduplication/actions/workflows/main-ci.yml/badge.svg)](https://github.com/tomasalmeida/dedu-permissions-deduplication/actions/workflows/main-ci.yml)

The idea of Dedu is allow any Apache Kafka operator to visualize the ACLs bindings in the cluster and perform cleaning if needed.

## Introduction
Dedu has three rules (more to come) to deduplicate the ACLs:
* Deleted Topic rule: as the deletion of a topic and an ACL binding can happen without sync, the cluster can keep old bindings that do not match any resource in the cluster.
* Redundant Binding rule: in ACL binding, the binding can be done LITERAL or PREFIXED. If a prefix binding also contains a literal one, the literal binding could be deleted.
  * Example: cluster has the rule READ for topic `topic-name-aaa` and also, a prefix READ rule for prefix `topic-name`, the literal rule is included in the prefixed rule.
* Consolidate literal bindings rule (experimental): several literal bindings can be consolidated in one prefixed binding rule.
  * More details how to activate this rule is provided below.
  * The reason of this feature is being experimental is the amount of memory and cpu needed to analyse the combinations to reduce the bindings.

## Usage

```shell
java -jar dedu-x.y.z.jar <parameters>
```

### All parameters

| Parameter                 | Required | Description                                                  |
|---------------------------|----------|--------------------------------------------------------------|
| --help                    |   NO     | Show all parameters                                          |
| --dedu-config-file <arg>  |   YES    | Dedu Config file path (with Dedu configuration properties)   |
| --kafka-config-file <arg> |   YES    | Kafka Config file path (with Kafka configuration properties) |
| --principal <arg>         |   NO     | Optimize permissions for this given principal                |

### Dedu config file properties

| Property                                               | Required | Accepted values                 | Default Value    | Description                                                                              |
|--------------------------------------------------------|----------|---------------------------------|------------------|------------------------------------------------------------------------------------------|
| log.level                                              | NO       | TRACE, DEBUG, INFO, WARN, ERROR | INFO             | Log output level                                                                         |
| acl.current.output.log.enable                          | NO       | true / false                    | false            | Output the current bindings to the output log                                            |
| acl.current.output.csv.enable                          | NO       | true / false                    | false            | Output the current bindings to a CSV file                                                |
| acl.current.output.csv.path                            | NO       | any path                        | ./current.csv    | Path to save the CSV current bindings                                                    |
| acl.actionable.output.log.enable                       | NO       | true / false                    | false            | Output the actionable bindings to the output log                                         |
| acl.actionable.output.csv.enable                       | NO       | true / false                    | false            | Output the actionable bindings a CSV file                                                |
| acl.actionable.output.csv.path                         | NO       | any path                        | ./actionable.csv | Path to save the CSV actionable bindings                                                 |
| rule.consolidate.literal.topic.bindings.enabled        | NO       | true / false                    | false            | Enable experimental rule to consolidate several literal bindings into one prefix binding |
| rule.consolidate.literal.topic.bindings.prefix.min     | NO       | number bigger than 3            | 3                | Minimum size of the prefix                                                               |
| rule.consolidate.literal.topic.bindings.replaced.min   | NO       | number bigger than 2            | 2                | Minimum number of replaced bindings to allow consolidation                               |

## Example

### For all principals
```shell
  mvn clean package
  cd examples/sasl-plain-cluster
  ./startCluster.sh
  cd ../..
  java -jar target/dedu-x.y.z.jar --kafka-config-file examples/sasl-plain-cluster/clients/kafka-user.properties --dedu-config-file examples/dedu.properties
```

### For one specific user
```shell
  mvn clean package
  cd examples/sasl-plain-cluster
  ./startCluster.sh
  cd ../..
  java -jar target/dedu-x.y.z.jar --kafka-config-file examples/sasl-plain-cluster/clients/kafka-user.properties --dedu-config-file examples/dedu.properties --principal User:alice
```


