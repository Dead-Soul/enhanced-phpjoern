package ast.php.statements;

import ast.expressions.Expression;
import ast.expressions.StringExpression;
import ast.logical.statements.Statement;

public class ConstantElement extends Statement
{
	private StringExpression name = null;
	private Expression value = null;
	private StringExpression docComment = null;

	public StringExpression getDocComment()
	{
		return this.docComment;
	}

	public void setDocComment(StringExpression docComment)
	{
		this.docComment = docComment;
		super.addChild(docComment);
	}

	public StringExpression getNameChild()
	{
		return this.name;
	}
	
	public void setNameChild(StringExpression name)
	{
		this.name = name;
		super.addChild(name);
	}
	
	public Expression getValue()
	{
		return this.value;
	}
	
	public void setValue(Expression value)
	{
		this.value = value;
		super.addChild(value);
	}
}
