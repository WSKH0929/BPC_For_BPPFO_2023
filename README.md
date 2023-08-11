# Instances and results of the BPPFO experiments in Wang et al. (2023)

### Full reference: Sunkanghong Wang, Shaowen Yao, Hao Zhang, Qiang Liu and Lijun Wei. (2023). A Branch-and-Price-and-Cut Algorithm for the Bin Packing Problem with Fragile Objects. Under revision.

This repository hosts the files backing up the data and experimental results described in our paper A Branch-and-Price-and-Cut Algorithm for the Bin Packing Problem with Fragile Objects referenced above. The paper is now under revision. A link to it will be provided as soon as possible.

If you have any questions, please feel free to reach out to **[villagerwei@gdut.edu.cn](mailto:villagerwei@gdut.edu.cn)** or **[wskh0929@gmail.com](mailto:wskh0929@gmail.com)**

## Instances

The BPPFI files that describe the instances are found in the **`instances`** folder. The instances in this folder are classic benchmark instances created by [**Clautiaux et al (2014)**](https://doi.org/10.1016/j.dam.2012.04.010). This benchmark set consists of 675 instances containing 50, 100 and 200 items, which are classified into 5 classes based on how they are created in terms of weights and fragilities.

## Results

The results CSV and OUT files are found inside the **`results`** folder. In **``results``**, you will find:

- The folder named **``out``** contains detailed packing patterns for all instances.
- The CSV file named **``results(19-011)``** contains calculated statistics for all instances. The meaning of each column is as follows:
  - **Name:** the name of the instance.
  - **Class:** the class of the instance.
  - **|N|:** the number of items in the instance.
  - **#Node:** the number of nodes explored in the BPC algorithm.
  - **#Node-P:** the number of nodes explored by the primal heuristic in the BPC algorithm.
  - **#Col:** the number of columns of the root node in the BPC algorithm.
  - **#PriceCnt:** the number of times the pricing subproblem is solved throughout the run of the BPC algorithm.
  - **LB0:** the initial lower bound $L_0$ for the BPC algorithm.
  - **LB:** the lower bound provided by the column generation procedure in the root node.
  - **UB0:** the initial upper bound computed using the greedy heuristic.
  - **UB:** the best upper bound found by the BPC algorithm.
  - **Opt:** it signifies whether the best upper bound found by the BPC algorithm is the optimal upper bound or not.
  - **Time:** the CPU seconds used to run the BPC algorithm.