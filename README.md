# Online Shop parallel shipping system for orders

An online shop `parallel` shipping system for **efficiently**
manage orders and products, written in `Java`.

## Workflow
`MAX` - the maximum number of threads which can simultaneously
run on a parallelization level

There are `MAX` `OrderThreads`, which are given an approximately equal
chunk of the total number of orders. This division is made by dividing
the total number of bytes (of the input order file) by `MAX`.\
These OrderThreads will invoke the `ItemsThreads` for each command,
so that it can be completed.

An ItemsThread will receive a command and will have to seek
all the corresponding items in the list. When all items are found and
marked as `shipped`, the ItemsThread finishes its task and
notifies the OrderThread, which can finally mark the whole command as shipped.

The process continues and new tasks are continuously added in the pool
until all orders are shipped (or, at least, marked).

## Parallelization
The parallelization was done firstly by using 2 `Thread Pool`.
One stands for the Order Workers, while the other one is for Items Workers.
The OrderThreads read the input file in parallel, as they each have
an according part of the document assigned by the bytes division.\
The ItemsThreads also read their input file in parallel,
each one having its own `BufferedReader` instance.\
The output write in files is `synchronized` by the use of `PrintStream`,
which is thread-safe, so there is no overwritten content in the documents. 

## License

[Adrian Croitoru](https://github.com/adriancroitoru97)