public class RecordPatternMatchSwitchImproved {


    public static void main(String[] args) {

    }


    sealed interface Order permits PhysicalOrder, DigitalOrder, ShippingOrder {

    }

    final class PhysicalOrder implements Order {
        @SuppressWarnings("unused")
        private int weight;
        @SuppressWarnings("unused")
        private int amount;

        public PhysicalOrder(int weight, int amount) {
            this.weight = weight;
            this.amount = amount;
        }
    }

    final class DigitalOrder implements Order {

        public DigitalOrder(int amount) {

        }
    }

    final class ShippingOrder implements Order {

    }
}
