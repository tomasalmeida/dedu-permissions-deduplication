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

| Property        | log.level                       |
|-----------------|---------------------------------|
| Required        | NO                              |
| Accepted values | TRACE, DEBUG, INFO, WARN, ERROR |
| Default Value   | INFO                            |
| Description     | Log output level                |

| Property        | acl.current.output.log.enable                 |
|-----------------|-----------------------------------------------|
| Required        | NO                                            |
| Accepted values | true / false                                  |
| Default Value   | false                                         |
| Description     | Output the current bindings to the output log |


| Property        | acl.current.output.csv.enable             |
|-----------------|-------------------------------------------|
| Required        | NO                                        |
| Accepted values | true / false                              |
| Default Value   | false                                     |
| Description     | Output the current bindings to a CSV file |

| Property        | acl.current.output.csv.path           |
|-----------------|---------------------------------------|
| Required        | NO                                    |
| Accepted values | any path                              |
| Default Value   | ./current.csv                         |
| Description     | Path to save the CSV current bindings |

| Property        | acl.actionable.output.log.enable                 |
|-----------------|--------------------------------------------------|
| Required        | NO                                               |
| Accepted values | true / false                                     |
| Default Value   | false                                            |
| Description     | Output the actionable bindings to the output log |

| Property        | xxx |
|-----------------|-----|
| Required        | xxx |
| Accepted values | xxx |
| Default Value   | xxx |
| Description     | xxx |


acl.actionable.output.csv.enable=true


| Property        | xxx |
|-----------------|-----|
| Required        | xxx |
| Accepted values | xxx |
| Default Value   | xxx |
| Description     | xxx |

acl.actionable.output.csv.path=target/actionable.csv


| Property        | xxx |
|-----------------|-----|
| Required        | xxx |
| Accepted values | xxx |
| Default Value   | xxx |
| Description     | xxx |

rule.consolidate.literal.topic.bindings.enabled=true


| Property        | xxx |
|-----------------|-----|
| Required        | xxx |
| Accepted values | xxx |
| Default Value   | xxx |
| Description     | xxx |

rule.consolidate.literal.topic.bindings.prefix.min=4


| Property        | xxx |
|-----------------|-----|
| Required        | xxx |
| Accepted values | xxx |
| Default Value   | xxx |
| Description     | xxx |

rule.consolidate.literal.topic.bindings.replaced.min=3

## Example 

--kafka-config-file examples/sasl-plain-cluster/clients/kafka-user.properties --dedu-config-file examples/dedu.properties

--kafka-config-file examples/sasl-plain-cluster/clients/kafka-user.properties --principal User:alice --dedu-config-file examples/dedu.properties