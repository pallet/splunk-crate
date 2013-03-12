# Release notes

## pallet-crates-0.7.0.alpha.1

- Add support for splunk 5.0.2 and correctly support splunk forwarders (in
  particular for splunk storm)

- Add support for universal forwarder install.
  Also: fix bug where rpm install didn't check for existence of previous
  install.

- Update parent POM; Fix crate.
  Parent POM is now 0.7.1 Crate works with new download link URLs for Splunk.
  Crate uses pallet.script.lib/tmp-dir. Add Splunk versions for 4.3.x and
  latest/major 4.2.x/4.1.x

- Add sonatype releases to ensure parent pom resolution

- Update pom for 0.7.0

- Add .gitignore file

## pallet-crates-0.5.0


## pallet-crates-0.4.4


## pallet-crates-0.4.3

- Update for 0.5.0-SNAPSHOT
  Change pallet.resource.* to pallet.action.*. Change stevedore calls to
  script functions to use unquote and the pallet.script.lib namespace. 
  Change request to session.  Change build-resources to build-actions.


## pallet-crates-0.4.2


## pallet-crates-0.4.1


## pallet-crates-0.4.0


## pallet-crates-0.4.0-beta-1

- Move node creation for tests in test-utils. Fix direct java style access to
  nodes from crates

- working node-list compute service

- Refactored to extract compute provider lib

- Updated to use template as a map, and for new Hardware in jclouds nodes

- Refactoring to a more functional implementation

- Added configuration for sending syslog output to splunk via fifo

- Added crate to install splunk (no configuration yet)

