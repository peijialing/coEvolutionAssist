from main import init_system
from api.apiutils import Relation
api, reporting = init_system("test/testmodel")
ori_table = “dblp.csv”
table_drs = api.drs_from_table(table)
pkfk_similar = api.pkfk_of(table_drs) 
table_list = [table_drs,pkfk_similar]
print(table_list)