N=1
exp_file=balana-exp.jar

echo "Params: true DELETE_POLICY"
for i in $( seq 1 $N )
do
  java -jar $exp_file true true DELETE_POLICY
done

echo ""
echo ""

echo "Params: true INSERT_POLICY"
for i in $( seq 1 $N )
do
  java -jar $exp_file true true INSERT_POLICY
done

echo ""
echo ""

echo "Params: true UPDATE_POLICY_DELETE_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file true true UPDATE_POLICY_DELETE_CONDITION
done

echo ""
echo ""

echo "Params: true UPDATE_POLICY_EDIT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file true true UPDATE_POLICY_EDIT_CONDITION
done

echo ""
echo ""

echo "Params: true UPDATE_POLICY_INSERT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file true true UPDATE_POLICY_INSERT_CONDITION
done

echo ""
echo ""

echo "Params: false DELETE_POLICY"
for i in $( seq 1 $N )
do
   java -jar $exp_file true false DELETE_POLICY
done

echo ""
echo ""

echo "Params: false INSERT_POLICY"
for i in $( seq 1 $N )
do
   java -jar $exp_file true false INSERT_POLICY
done

echo ""
echo ""

echo "Params: false UPDATE_POLICY_DELETE_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file true false UPDATE_POLICY_DELETE_CONDITION
done

echo ""
echo ""

echo "Params: false UPDATE_POLICY_EDIT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file true false UPDATE_POLICY_EDIT_CONDITION
done

echo ""
echo ""

echo "Params: false UPDATE_POLICY_INSERT_CONDITION"
for i in $( seq 1 $N )
do
  java -jar $exp_file true false UPDATE_POLICY_INSERT_CONDITION
done

echo ""
echo ""