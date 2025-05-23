# Instances and results of the BPPFO experiments in Wang et al. (2025)

### Full reference: Sunkanghong Wang, Shaowen Yao, Hao Zhang, Qiang Liu and Lijun Wei. (2025). Arc-flow formulation and branch-and-price-and-cut algorithm for the bin-packing problem with fragile objects. Computers & Operations Research, 173, 106878.

This repository hosts the files backing up the data and experimental results described in our paper (https://doi.org/10.1016/j.cor.2024.106878) referenced above.

If you have any questions, please feel free to reach out to **[villagerwei@gdut.edu.cn](mailto:villagerwei@gdut.edu.cn)** or **[wskh0929@gmail.com](mailto:wskh0929@gmail.com)**

## Cite

To cite the contents of this repository, please cite both the paper and this repo.

Below is the BibTex of the paper:

```
@article{WANG2025106878,
title = {Arc-flow formulation and branch-and-price-and-cut algorithm for the bin-packing problem with fragile objects},
journal = {Computers \& Operations Research},
volume = {173},
pages = {106878},
year = {2025},
issn = {0305-0548},
doi = {https://doi.org/10.1016/j.cor.2024.106878},
url = {https://www.sciencedirect.com/science/article/pii/S0305054824003502},
author = {Sunkanghong Wang and Shaowen Yao and Hao Zhang and Qiang Liu and Lijun Wei}
}
```

## Code

In the **`code_BP_2013`** folder, you will find the source code of the branch-and-price algorithm we implemented from [**Martínez et al. (2013)**](https://doi.org/10.1016/j.disopt.2013.06.001).

## Instances
### Existing_Instances

The BPPFI files that describe the instances are found in the **`instances/Existing_Instances`** folder. The instances in this folder are classic benchmark instances created by [**Clautiaux et al. (2014)**](https://doi.org/10.1016/j.dam.2012.04.010). This benchmark set consists of 675 instances containing 50, 100 and 200 items, which are classified into 5 classes based on how they are created in terms of weights and fragilities.

### Random_Instances

The second set of instances is generated randomly using uniform distributions, which can be found in the **`instances/Random_Instances`** folder. This group of instances can be represented by a tuple $(c,n,R)$:

- The value of $c$ corresponds to the maximum fragility value among all items, and we consider $c\in \{10,50,100\}$.
- The value of $n$ corresponds to the number of items in the instance, and we consider $n\in \{200,300,400\}$.
- The value of $R$ corresponds to the range for the fragility of items relative to $c$, and we consider $R\in \{[0.1,1.0],[0.5,1.0]\}$.

We ensure that $C=c$ and $|N|=n$ for each randomly generated instance. Given a tuple $(c,n,R)$, the construction rules for the instance are as follows:

- The first $n-1$ items have fragility $f = \lceil c \times U(R) \rceil$, where $U(R)$ represents the real number uniformly random in $R$. The last item has fragility $f=c$
- For each item with known fragility value $f$, we uniformly randomize an integer in $[1,f]$ as its weight.

Based on the above rules, we generate $10$ instances for each tuple, resulting in $180$ total instances.

## Results

### 2013_Results

In the **`results/2013_Results`** folder, you will find the results of BP(2013):

- The folder named **``log``** contains the log information output during algorithm operation for all instances.
- The folder named **``solutions``** contains detailed packing patterns for all instances. 
- The CSV file named **``Res-2024-05-07``** contains calculated statistics for all instances.

### Our_Results

The results CSV and OUT files are found inside the **`results/Our_Results`** folder. In the folder, you will find the results of our BPC:

- The folder named **``out``** contains detailed packing patterns for all instances.
- The CSV file named **``results(19-011)``** contains calculated statistics for all instances. The meaning of each column is as follows:
  - **Name:** the name of the instance,
  - **Class:** the class of the instance,
  - **|N|:** the number of items in the instance,
  - **#Node:** the number of nodes explored in the BPC algorithm,
  - **#Node-P:** the number of nodes explored by the primal heuristic in the BPC algorithm,
  - **#Col:** the number of columns of the root node in the BPC algorithm,
  - **#PriceCnt:** the number of times the pricing subproblem is solved throughout the run of the BPC algorithm,
  - **LB0:** the initial lower bound $L_0$ for the BPC algorithm,
  - **LB:** the lower bound provided by the column generation procedure in the root node,
  - **UB0:** the initial upper bound computed using the greedy heuristic,
  - **UB:** the best upper bound found by the BPC algorithm,
  - **Opt:** it signifies whether the best upper bound found by the BPC algorithm is the optimal upper bound or not,
  - **Time:** the CPU seconds used to run the BPC algorithm.
