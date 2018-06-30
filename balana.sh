N=10
exp_file=balana-exp-time.jar

echo "Params: true DELETE_POLICY"
for i in $( seq 1 $N )
do
  java -jar $exp_file false true DELETE_POLICY
done

echo ""
echo ""

echo "Params: true INSERT_POLICY"
for i in $( seq 1 $N )
do
  java -jar $exp_file false true INSERT_POLICY
done

echo ""
echo ""

echo "Params: true UPDATE_POLICY_DELETE_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file false true UPDATE_POLICY_DELETE_CONDITION
done

echo ""
echo ""

echo "Params: true UPDATE_POLICY_EDIT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file false true UPDATE_POLICY_EDIT_CONDITION
done

echo ""
echo ""

echo "Params: true UPDATE_POLICY_INSERT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file false true UPDATE_POLICY_INSERT_CONDITION
done

echo ""
echo ""

echo "Params: false DELETE_POLICY"
for i in $( seq 1 $N )
do
   java -jar $exp_file false false DELETE_POLICY
done

echo ""
echo ""

echo "Params: false INSERT_POLICY"
for i in $( seq 1 $N )
do
   java -jar $exp_file false false INSERT_POLICY
done

echo ""
echo ""

echo "Params: false UPDATE_POLICY_DELETE_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file false false UPDATE_POLICY_DELETE_CONDITION
done

echo ""
echo ""

echo "Params: false UPDATE_POLICY_EDIT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file false false UPDATE_POLICY_EDIT_CONDITION
done

echo ""
echo ""

echo "Params: false UPDATE_POLICY_INSERT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file false false UPDATE_POLICY_INSERT_CONDITION
done

echo ""
echo ""