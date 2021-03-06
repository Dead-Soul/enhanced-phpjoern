package ast.php.expressions;

import ast.ASTNodeProperties;
import ast.expressions.CallExpressionBase;
import ast.expressions.Expression;

public class MethodCallExpression extends CallExpressionBase
{
	private Expression targetObject = null;
	
	public Expression getTargetObject()
	{
		return this.targetObject;
	}
	
	public void setTargetObject(Expression targetObject)
	{
		this.targetObject = targetObject;
		super.addChild(targetObject);
	}

	private long classid = -1;
	public long getClassid(){return classid;}
	public void setClassid(long id){classid = id;}

	// useless in ast.so 70
	public String getEnclosingClass() {
		return getProperty(ASTNodeProperties.CLASSNAME);
	}
	
	public void setEnclosingClass(String classname) {
		setProperty(ASTNodeProperties.CLASSNAME, classname);
	}
}
