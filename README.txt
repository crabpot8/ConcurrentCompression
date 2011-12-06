How to Run:

Simply run the Main.class file.

Inputs:

Any file in the "data/in" directory will be used as input for the compression process

Outputs:

Since we route all writing operations to an empty writer, the compressed versions of the input images are not actually output.

Three comma-delimited files are produced as output:

    - full_time.csv
        This records the average time needed for the original algorithm to run for an image compared to our multi-threaded implementation over 1-10 worker threads

    - color_conversion_timings.csv
        Records averages for the color conversion section of the code alone

    - write_compressed_data_timings.csv
        Records averages for the DCT / Huffman encoding section of the code alone
