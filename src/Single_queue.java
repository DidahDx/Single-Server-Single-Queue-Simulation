
public class Single_queue {
   private final int Q_LIMIT = 100; /* Limit on the queue length */
   private final int BUSY = 1; /* When server is being busy */
   private final int IDLE = 0; /* When server is idle */

   private int next_event_type = 1;
   private int num_custs_delayed = 0, num_events;

   private int num_in_q = 0;
   private int server_status = 0;

   private double area_num_in_q = 0;
   private double area_server_status = 0;

   /*setting mean inter arrival time to 1*/
   private double mean_interarrival = 1;
   /*setting mean service time 0.5*/
   private double mean_service = 0.5;
   /*setting number of runs to 1000*/
   private int num_delays_required=1000;

   private double sim_time = 0;
   private double time_last_event,total_of_delays;

   private final double[] time_arrival = new double[Q_LIMIT + 1];
   private final double[] time_next_event = new double[3];


   /*starting the program*/
   public static void main(String[] args) {
      Single_queue single= new Single_queue();

      /*starting the simulation*/
      single.simulate();
   }

   private StringBuilder buffer = new StringBuilder();

   private void simulate(){
        /* Specify the number of events for the
        timing function */
      num_events = 2;



      /* Write report heading and input parameters */
      buffer.append("Single server single queue system\n\n");
      buffer.append(String.format("Mean interarrival time%11.3f"
              + " minutes \n\n", mean_interarrival));
      buffer.append(String.format("Mean service time%16.3f"
              + " minutes\n\n", mean_service));
      buffer.append(String.format("Number of runs%14d"
              + "\n\n", num_delays_required));

      /*Initialize the simulation */
      initialize();

      /* Run the simulation while more delays are still needed */
      while(num_custs_delayed <  num_delays_required){
         /*Determine the next event */
         timing();

         /* Update time-average statistical accumulators. */
         update_time_avg_stats();

         /* Invoke the appropriate event function */
         switch(next_event_type){
            case 1:
               arrive();
               break;
            case 2:
               depart();
               break;
         }
      }

      /* Invoke the report generator and end the simulation */
      report();

      print_output();

   }



   private void initialize(){
      /* Initialize the simulation clock. */
      sim_time = 0;

      /* Initialize the state variables. */
      server_status   = IDLE;
      num_in_q        = 0;
      time_last_event = 0.0;

      /* Initialize the statistical counters. */
      num_custs_delayed  = 0;
      total_of_delays    = 0.0;
      area_num_in_q      = 0.0;
      area_server_status = 0.0;

	/* Initialize the event list. Since no customers
        are present, the departure (service completion)
        event is eliminated from consideration. */
      time_next_event[1] = sim_time + expon(mean_interarrival);
      time_next_event[2] = 1.0e+30;
   }

   private void timing()
   {
      int i;
      double min_time_next_event = 1.0e+29;

      next_event_type = 0;

      /* Determine the event type for the next event to occur. */
      for(i = 1; i <= num_events; ++i){
         if(time_next_event[i] < min_time_next_event){
            min_time_next_event = time_next_event[i];
            next_event_type = i;
         }
      }

      /* Check to see whether the event list is empty. */
      if(next_event_type == 0){
         /* The event list is empty, so stop the simulation. */
         buffer.append(String.format("\nEvent list is empty "
                 + "at time %f", sim_time));
         print_output();
         System.exit(1);
      }

      /* The event list is not empty, so advance the simulation clock. */
      sim_time = min_time_next_event;
   }

   private void arrive()
   {
      double delay;
      /* Schedule next arrival. */
      time_next_event[1] = sim_time + expon(mean_interarrival);

      /* Check to see whether server is busy. */
      if(server_status == BUSY){
         /* Server is busy so increment the number of customers in the queue. */
         ++num_in_q;

         /* Check to see whether an overflow condition exists. */
         if(num_in_q > Q_LIMIT){
            /* The queue has overflowed, so stop the simulation. */
            buffer.append("\nOverflow of the array time_arrival at");
            buffer.append(String.format(" time %f", sim_time));
            System.exit(2);
         }

            /* There is still room in the queue, so store the time
            of arrival of the arriving customer at the (new) end of time_arrival. */
         time_arrival[num_in_q] = sim_time;
      }

      else{
         /* Server is idle, so arriving customer has a delay of zero. */
         delay = 0.0;
         total_of_delays += delay;

         /* Increment the number of customers delayed, and make server busy. */
         ++num_custs_delayed;
         server_status = BUSY;

         /* Schedule a departure (service completion). */
         time_next_event[2] = sim_time + expon(mean_service);
      }
   }

   private void depart()
   {
      int i;
      double delay;

      /* Check to see whether the queue is empty. */
      if(num_in_q == 0){
         /* The queue is empty so make the server idle and eliminate the
          * departure (service completion) event from consideration. */
         server_status = IDLE;
         time_next_event[2] = 1.0e+30;
      }
      else{
            /* The queue is nonempty, so decrement the
            number of customers in queue. */
         --num_in_q;

         /* Compute the delay of the customer who is beginning service
          * and update the total delay of accumulator. */
         delay = sim_time - time_arrival[1];
         total_of_delays += delay;

            /* Increment the number of customers delayed,
            and schedule departure. */
         ++num_custs_delayed;
         time_next_event[2] = sim_time + expon(mean_service);

         /* Move each customer in queue (if any) up one place. */
         for(i = 1; i <= num_in_q; ++i){
            time_arrival[i] = time_arrival[i + 1];
         }
      }
   }

   private void report()
   {
      /* Compute and write estimates of desired measures of performance. */
      buffer.append(String.format("\nAverage delay in queue%11.3f"
              + " minutes\n\n", total_of_delays / num_custs_delayed));
      buffer.append(String.format("\nAverge number of customers in "
              + "queue%10.3f\n\n", area_num_in_q / sim_time));
      buffer.append(String.format("Time the server is "
              + "busy%15.3f\n\n", area_server_status / sim_time));
      buffer.append(String.format( " Time simulation ended"
              + "%12.3f minutes", sim_time));
   }

   private void update_time_avg_stats()
   {
      double time_since_last_event;

      /* Compute time since last event, and update last-event-time- marker. */
      time_since_last_event = sim_time - time_last_event;
      time_last_event = sim_time;

      /* Update area under number-in-queue function */
      area_num_in_q += num_in_q * time_since_last_event;

      /* Update area under server-busy indicator function. */
      area_server_status += server_status * time_since_last_event;
   }

   /* Printing output of the results*/
   private void print_output(){

      System.out.println(buffer.toString());

   }

   private double expon(double mean)
   {
      return -mean *Math.log(Math.random());
   }

}
