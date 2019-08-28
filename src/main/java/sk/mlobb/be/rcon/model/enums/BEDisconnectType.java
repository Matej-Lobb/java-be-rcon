package sk.mlobb.be.rcon.model.enums;

public enum BEDisconnectType {
    Manual {
        public String toString() {
            return "Manual disconnect";
        }
    },

    ConnectionLost {
        public String toString() {
            return "Connection lost (possible timeout)";
        }
    },

    SocketException {
        public String toString() {
            return "Connection lost due to exception";
        }
    }
}