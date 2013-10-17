import py4j.GatewayServer;

public class DebugEntryPoint {

    private Debug debug;

    public DebugEntryPoint() {
      debug = new Debug();
    }

    public Debug getDebugger() {
        return debug;
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new DebugEntryPoint());
        gatewayServer.start();
        System.out.println("Debug Server Started");
    }

}
