# work-in-progress pattern

- a pattern for non-blocking multi-threaded programming
- based on work stealing and atomic operations

#### An algorithm

- Thread A starts processing a request
- Thread B comes in and figures out that another thread has already been in a process and there 
are two situations that can happen:
    - Thread B spins and tries to put a job to a Thread A or 
    - Thread A makes leaving the process and Thread B needs to make a step forward and process the job itself.