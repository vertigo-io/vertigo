package io.vertigo.core.node.component.data;

public class ConnectorA implements SomeTypeOfConnector {
	@Override
	public String getClient() {
		return "hello A";
	}
}
