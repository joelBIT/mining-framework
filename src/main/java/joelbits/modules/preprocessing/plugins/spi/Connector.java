package joelbits.modules.preprocessing.plugins.spi;

import joelbits.modules.preprocessing.connectors.DataCollector;
import joelbits.modules.preprocessing.connectors.RevisionProcessible;
import joelbits.modules.preprocessing.connectors.SnapshotSwitchable;
import joelbits.modules.preprocessing.connectors.Connectible;

public interface Connector extends Connectible, DataCollector, RevisionProcessible, SnapshotSwitchable {
}
