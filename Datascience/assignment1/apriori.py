import sys
from itertools import combinations

#입력값 검사 및 입력 받기
if len(sys.argv) != 4:
    print("check your arguments")
    sys.exit()
try:
    minimum_s = float(sys.argv[1])
except ValueError:
    print("Invalid minumum_suport value")
    sys.exit()
input_file_name = sys.argv[2]
output_file_name = sys.argv[3]


# input file 읽어서 transactions에 저장
with open(input_file_name) as input_file:
    transactions = [set(map(int, line.split('\t'))) for line in input_file]
    

# 초기 itemsets 생성하는 함수
def get_itemsets(transactions):
        itemsets = set()
        for transaction in transactions:
            for item in transaction:
                itemsets.add(frozenset([item]))
        return itemsets

# minimum support값을 기준으로 freqent itemsets를 생성하는 함수
def get_freq_itemsets(itemsets, transactions):
        item_counts = dict()
        freq_itemsets = set()

        for transaction in transactions:
            for itemset in itemsets:
                if itemset.issubset(transaction):
                    try :
                        item_counts[itemset] += 1
                    except KeyError:
                         item_counts[itemset] = 1

        for itemset in item_counts:
            support = (item_counts[itemset] / float(len(transactions)))*100
            if support >= minimum_s:
                freq_itemsets.add(itemset)

        return freq_itemsets

# frequent itemsets로 union해서 새로운 itemsets 생성하는 함수
def get_new_itemsets(freq_itemsets):
        new_itemsets = set()
        for itemset1 in freq_itemsets:
            for itemset2 in freq_itemsets:
                union = itemset1.union(itemset2)
                if len(union) == len(itemset1) + 1:
                    new_itemsets.add(union)
        return new_itemsets


#support 값 구하는 함수
def get_support(itemset):
    return sum(1 for transaction in transactions if itemset.issubset(transaction))


#생성된 itemsets, freqent itemsets를 바탕으로 association rule을 생성하는 함수
def get_rules(itemset, freq_itemsets, total_len):
    rules = []
    for k in freq_itemsets:
        if k == 1:
            continue
        for itemset in freq_itemsets[k]:
            for item in itemset:
                for i in range(1, k):
                    for antecedent in combinations(itemset - frozenset([item]), i):
                        consequent = itemset - frozenset(antecedent)
                        if set(antecedent).issubset(itemset) and set(consequent).issubset(itemset):
                            s = round((get_support(itemset) / (total_len))*100,2)
                            confidence = round((get_support(itemset) / get_support(frozenset(antecedent)))*100,2)
                            rules.append((frozenset(antecedent), (consequent), s, confidence))
    return rules


#freqent itemsets 생성, 초기 itemsets 생성
freq_itemsets = {}
itemsets = get_itemsets(transactions)


#freqent itemsets를 size 증가시켜서 더이상 나오지 않을 때까지 생성
k = 1
while True:
    if k == 1:
        freq_itemsets[k] = get_freq_itemsets(itemsets, transactions)
    else :
        new_item_sets = get_new_itemsets(freq_itemsets[k - 1])
        freq_itemsets[k] = get_freq_itemsets(new_item_sets, transactions)
    if not freq_itemsets[k]:
        break
    k += 1


#association rule 얻어서 output.txt생성
rules = set(get_rules(new_item_sets, freq_itemsets,len(transactions)))
with open(output_file_name,"w") as output_file:
    while rules:
        rule = rules.pop()
        antecedent = ",".join(map(str, rule[0]))
        consequent = ",".join(map(str, rule[1]))
        
        result = "{%s}\t{%s}\t%.2f\t%.2f\n" %(antecedent,consequent,rule[2],rule[3])
        output_file.write(result)
