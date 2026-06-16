
public class CoinFrequencies {


    public static void main(String[] args) {
        //System.out.println("Hello World!");
        // add customers with different rupees denominations
        // if not 5/10/20 reject
            // if balance to return present accept or reject.
        // while accepting request, check balance of coin frequencies to return settlement - if present accept request
        // prepare and serve product
        //
        int[] customerRequests = {5,5,10,20,5,5,5,20};
        validateRequests(customerRequests);
    }


    public static void validateRequests(int[] customerRequests) {
        int count5 = 0;
        int count10 = 0;
        @SuppressWarnings("unused")
        int count20 = 0;

        for(int i =0; i< customerRequests.length; i++) {

            if(customerRequests[i] == 5 ) {
                count5++;
                System.out.println("Request served at position with amount:" + i + " - " + customerRequests[i]
                                    +". No amount returned. count5 freq: "+ count5);
                continue;
            } else if ( customerRequests[i] == 10 && count5 > 0 ) {
                count10++;
                count5--;
                System.out.println("Request accepted and served at position with amount:" + i + " - " + customerRequests[i]
                        +". Amount returned. 5");
                continue;
            } else {
                // 15 return required. check count of 10 if greater than 1 and count of 5 greater than 1
                if (customerRequests[i] - 5 == 15) {
                    // check to return 15 10 note and 5 coin present.
                    if (count10 > 0 && count5 > 0) {
                        count20++;
                        count5--;
                        count10--;
                        System.out.println("Request accepted and served at position with amount:" + i + " - " + customerRequests[i]
                                +". Amount returned. 5, 10");
                        continue;
                    } // check to return 5 coins available
                    else if (count5 > 2) {
                        count20++;
                        count5--;
                        count5--;
                        count5--;
                        System.out.println("Request accepted and served at position with amount:" + i + " - " + customerRequests[i]
                                +". Amount returned. 5*3");
                        continue;
                    } else {
                        System.err.println("Invalid request at position. " + i + " for value: "
                                + customerRequests[i] + " balance freq count5:" + count5 + " count10:" + count10);
                    }
                }
            }
        }
    }
}
