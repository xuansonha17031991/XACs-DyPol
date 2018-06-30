N=10
exp_file=balana-exp-time.jar

echo "Params: false UPDATE_POLICY_DELETE_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file true false UPDATE_POLICY_DELETE_CONDITION
done

# echo ""
# echo ""

# echo "Params: false UPDATE_POLICY_EDIT_CONDITION"
# for i in $( seq 1 $N )
# do
#   java -jar $exp_file true false UPDATE_POLICY_EDIT_CONDITION
# done

# echo ""
# echo ""

# echo "Params: true UPDATE_POLICY_DELETE_CONDITION"
# for i in $( seq 1 $N )
# do
#   java -jar $exp_file true true UPDATE_POLICY_DELETE_CONDITION
# done

# echo ""
# echo ""

# echo "Params: true UPDATE_POLICY_EDIT_CONDITION"
# for i in $( seq 1 $N )
# do
#   java -jar $exp_file true true UPDATE_POLICY_EDIT_CONDITION
# done

# echo ""
# echo ""