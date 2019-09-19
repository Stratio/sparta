/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models.qualityrule

import com.stratio.sparta.core.models.qualityrule.operations._
import com.stratio.sparta.core.models.{SpartaQualityRule, SpartaQualityRulePredicate}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DecimalType, _}

import scala.math.Ordering


class SparkQualityRule[U <: Row](spartaQualityRule: SpartaQualityRule,
                                 schemaExtracted: StructType) extends Serializable {

  lazy val id: Long = spartaQualityRule.id

  def noOpAnd: Function1[U,Boolean] = (_: U) => true
  def noOpOr: Function1[U,Boolean] = (_: U) => false

  def composeAnd( f1: => Function1[U,Boolean], f2: => Function1[U,Boolean]):
  Function1[U,Boolean] = (x: U) => f1(x) && f2(x)

  def composeOr( f1: => Function1[U,Boolean], f2: => Function1[U,Boolean]):
  Function1[U,Boolean] = (x: U) => f1(x) || f2(x)


  def functionComposed(func: => Seq[Function1[U,Boolean]], logicalOperator: String): Function1[U, Boolean] =
    logicalOperator match {
      case s if s matches "(?i)and" => func.foldLeft(noOpAnd){(agg, el) => composeAnd(agg, el)}
      case s if s matches "(?i)or" => func.foldLeft(noOpOr){(agg, el) => composeOr(agg, el)}
    }

  val predicates: Seq[SparkPredicate[U]] = spartaQualityRule.predicates.map(predicate => new SparkPredicate[U](predicate, schemaExtracted)).filter(_.valid)

  val functionSequence: Seq[Function1[U,Boolean]] = predicates.map(_.getOperation)

  val composedPredicates: Function1[U,Boolean] = functionComposed(functionSequence, spartaQualityRule.logicalOperator.get)
}


class SparkPredicate[U <: Row](spartaQualityRulePredicate : SpartaQualityRulePredicate,
                               schemaExtracted: StructType) extends Serializable {

  lazy val field: String = spartaQualityRulePredicate.field

  lazy val fieldType: DataType = schemaExtracted.apply(field).dataType

  lazy val ruleOperand: String = spartaQualityRulePredicate.operation

  val allowedOperations: Set[String] = Set("=", ">", ">=","<", "<=", "<>", "IS NOT NULL", "IS NULL", "IN", "NOT IN", "LIKE", "NOT LIKE", "REGEX", "IS DATE", "IS NOT DATE", "IS TIMESTAMP", "IS NOT TIMESTAMP")

  def valid = if (allowedOperations.contains(ruleOperand.trim.toUpperCase)) true else false

  implicit val spartaPredicate: SpartaQualityRulePredicate = spartaQualityRulePredicate
  implicit val schema: StructType = schemaExtracted

  def getOperation: U => Boolean = {
    ruleOperand match {
      case "=" =>
        val equalOperation = fieldType match {
          case StringType => new EqualOperation[String, U](Ordering[String])
          case BooleanType => new EqualOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new EqualOperation[Int, U](Ordering[Int])
          case LongType => new EqualOperation[Long, U](Ordering[Long])
          case ShortType => new EqualOperation[Short, U](Ordering[Short])
          case FloatType => new EqualOperation[Float, U](Ordering[Float])
          case DoubleType => new EqualOperation[Double, U](Ordering[Double])
          case x: DecimalType => new EqualOperation[Any, U](new DecimalOrdering(x))
          case _ => new EqualOperation[String, U](Ordering[String])
        }
        equalOperation.operation
      case "<>" =>
        val notEqualOperation = fieldType match {
          case StringType => new NotEqualOperation[String, U](Ordering[String])
          case BooleanType => new NotEqualOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new NotEqualOperation[Int, U](Ordering[Int])
          case LongType => new NotEqualOperation[Long, U](Ordering[Long])
          case ShortType => new NotEqualOperation[Short, U](Ordering[Short])
          case FloatType => new NotEqualOperation[Float, U](Ordering[Float])
          case DoubleType => new NotEqualOperation[Double, U](Ordering[Double])
          case x: DecimalType => new NotEqualOperation[Any, U](new DecimalOrdering(x))
          case _ => new NotEqualOperation[String, U](Ordering[String])
        }
        notEqualOperation.operation
      case ">" =>
        val greaterOperation = fieldType match {
          case StringType => new GreaterOperation[String, U](Ordering[String])
          case BooleanType => new GreaterOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new GreaterOperation[Int, U](Ordering[Int])
          case LongType => new GreaterOperation[Long,U](Ordering[Long])
          case ShortType=> new GreaterOperation[Short,U](Ordering[Short])
          case FloatType => new GreaterOperation[Float,U](Ordering[Float])
          case DoubleType => new GreaterOperation[Double,U](Ordering[Double])
          case x: DecimalType => new GreaterOperation[Any, U](new DecimalOrdering(x))
          case _ => new GreaterOperation[String, U](Ordering[String])
        }
       greaterOperation.operation
      case ">=" =>
        val greaterEqualOperation = fieldType match {
          case StringType => new GreaterEqualOperation[String, U](Ordering[String])
          case BooleanType => new GreaterEqualOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new GreaterEqualOperation[Int, U](Ordering[Int])
          case LongType => new GreaterEqualOperation[Long,U](Ordering[Long])
          case ShortType=> new GreaterEqualOperation[Short,U](Ordering[Short])
          case FloatType => new GreaterEqualOperation[Float,U](Ordering[Float])
          case DoubleType => new GreaterEqualOperation[Double,U](Ordering[Double])
          case x: DecimalType => new GreaterEqualOperation[Any, U](new DecimalOrdering(x))
          case _ => new GreaterEqualOperation[String, U](Ordering[String])
        }
        greaterEqualOperation.operation
      case "<" =>
        val minorOperation = fieldType match {
          case StringType => new MinorOperation[String, U](Ordering[String])
          case BooleanType => new MinorOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new MinorOperation[Int, U](Ordering[Int])
          case LongType => new MinorOperation[Long,U](Ordering[Long])
          case ShortType=> new MinorOperation[Short,U](Ordering[Short])
          case FloatType => new MinorOperation[Float,U](Ordering[Float])
          case DoubleType => new MinorOperation[Double,U](Ordering[Double])
          case x: DecimalType => new MinorOperation[Any, U](new DecimalOrdering(x))
          case _ => new MinorOperation[String, U](Ordering[String])
        }
        minorOperation.operation
      case "<=" =>
        val minorEqualOperation = fieldType match {
          case StringType => new MinorEqualOperation[String, U](Ordering[String])
          case BooleanType => new MinorEqualOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new MinorEqualOperation[Int, U](Ordering[Int])
          case LongType => new MinorEqualOperation[Long,U](Ordering[Long])
          case ShortType=> new MinorEqualOperation[Short,U](Ordering[Short])
          case FloatType => new MinorEqualOperation[Float,U](Ordering[Float])
          case DoubleType => new MinorEqualOperation[Double,U](Ordering[Double])
          case x: DecimalType => new MinorEqualOperation[Any, U](new DecimalOrdering(x))
          case _ => new MinorEqualOperation[String, U](Ordering[String])
        }
        minorEqualOperation.operation
      case s if s matches "(?i)in" =>
        val inOperation = fieldType match {
          case StringType => new InOperation[String, U](Ordering[String])
          case BooleanType => new InOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new InOperation[Int, U](Ordering[Int])
          case LongType => new InOperation[Long,U](Ordering[Long])
          case ShortType=> new InOperation[Short,U](Ordering[Short])
          case FloatType => new InOperation[Float,U](Ordering[Float])
          case DoubleType => new InOperation[Double,U](Ordering[Double])
          case x: DecimalType => new InOperation[Any, U](new DecimalOrdering(x))
          case _ => new InOperation[String, U](Ordering[String])
        }
        inOperation.operation
      case s if s matches "(?i)not in" =>
        val notInOperation = fieldType match {
          case StringType => new NotInOperation[String, U](Ordering[String])
          case BooleanType => new NotInOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new NotInOperation[Int, U](Ordering[Int])
          case LongType => new NotInOperation[Long,U](Ordering[Long])
          case ShortType=> new NotInOperation[Short,U](Ordering[Short])
          case FloatType => new NotInOperation[Float,U](Ordering[Float])
          case DoubleType => new NotInOperation[Double,U](Ordering[Double])
          case x: DecimalType => new NotInOperation[Any, U](new DecimalOrdering(x))
          case _ => new NotInOperation[String, U](Ordering[String])
        }
        notInOperation.operation
      case s if s matches "(?i)is null" =>
        val isNullOperation = fieldType match {
          case StringType => new IsNullOperation[String, U](Ordering[String])
          case BooleanType => new IsNullOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new IsNullOperation[Int, U](Ordering[Int])
          case LongType => new IsNullOperation[Long,U](Ordering[Long])
          case ShortType=> new IsNullOperation[Short,U](Ordering[Short])
          case FloatType => new IsNullOperation[Float,U](Ordering[Float])
          case DoubleType => new IsNullOperation[Double,U](Ordering[Double])
          case x: DecimalType => new IsNullOperation[Any, U](new DecimalOrdering(x))
          case _ => new IsNullOperation[String, U](Ordering[String])
        }
        isNullOperation.operation
      case s if s matches "(?i)is not null" =>
        val notNullOperation = fieldType match {
          case StringType => new NotNullOperation[String, U](Ordering[String])
          case BooleanType => new NotNullOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new NotNullOperation[Int, U](Ordering[Int])
          case LongType => new NotNullOperation[Long,U](Ordering[Long])
          case ShortType=> new NotNullOperation[Short,U](Ordering[Short])
          case FloatType => new NotNullOperation[Float,U](Ordering[Float])
          case DoubleType => new NotNullOperation[Double,U](Ordering[Double])
          case x: DecimalType => new NotNullOperation[Any, U](new DecimalOrdering(x))
          case _ => new NotNullOperation[String, U](Ordering[String])
        }
        notNullOperation.operation
      case s if s matches "(?i)like" =>
        val likeOperation = fieldType match {
          case StringType => new LikeOperation[String, U](Ordering[String])
          case BooleanType => new LikeOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new LikeOperation[Int, U](Ordering[Int])
          case LongType => new LikeOperation[Long,U](Ordering[Long])
          case ShortType=> new LikeOperation[Short,U](Ordering[Short])
          case FloatType => new LikeOperation[Float,U](Ordering[Float])
          case DoubleType => new LikeOperation[Double,U](Ordering[Double])
          case x: DecimalType => new LikeOperation[Any, U](new DecimalOrdering(x))
          case _ => new LikeOperation[String, U](Ordering[String])
        }
        likeOperation.operation
      case s if s matches "(?i)not like" =>
        val notLikeOperation = fieldType match {
          case StringType => new NotLikeOperation[String, U](Ordering[String])
          case BooleanType => new NotLikeOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new NotLikeOperation[Int, U](Ordering[Int])
          case LongType => new NotLikeOperation[Long,U](Ordering[Long])
          case ShortType=> new NotLikeOperation[Short,U](Ordering[Short])
          case FloatType => new NotLikeOperation[Float,U](Ordering[Float])
          case DoubleType => new NotLikeOperation[Double,U](Ordering[Double])
          case x: DecimalType => new NotLikeOperation[Any, U](new DecimalOrdering(x))
          case _ => new NotLikeOperation[String, U](Ordering[String])
        }
        notLikeOperation.operation
      case s if s matches "(?i)regex" =>
        val regexOperation = fieldType match {
          case StringType => new RegexOperation[String, U](Ordering[String])
          case BooleanType => new RegexOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new RegexOperation[Int, U](Ordering[Int])
          case LongType => new RegexOperation[Long,U](Ordering[Long])
          case ShortType=> new RegexOperation[Short,U](Ordering[Short])
          case FloatType => new RegexOperation[Float,U](Ordering[Float])
          case DoubleType => new RegexOperation[Double,U](Ordering[Double])
          case x: DecimalType => new RegexOperation[Any, U](new DecimalOrdering(x))
          case _ => new RegexOperation[String, U](Ordering[String])
        }
        regexOperation.operation
      case s if s matches "(?i)is date" =>
        val isDateOperation = fieldType match {
          case StringType => new IsDateOperation[String, U](Ordering[String])
          case BooleanType => new IsDateOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new IsDateOperation[Int, U](Ordering[Int])
          case LongType => new IsDateOperation[Long,U](Ordering[Long])
          case ShortType=> new IsDateOperation[Short,U](Ordering[Short])
          case FloatType => new IsDateOperation[Float,U](Ordering[Float])
          case DoubleType => new IsDateOperation[Double,U](Ordering[Double])
          case x: DecimalType => new IsDateOperation[Any, U](new DecimalOrdering(x))
          case _ => new IsDateOperation[String, U](Ordering[String])
        }
        isDateOperation.operation
      case s if s matches "(?i)is not date" =>
        val isNotDateOperation = fieldType match {
          case StringType => new IsNotDateOperation[String, U](Ordering[String])
          case BooleanType => new IsNotDateOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new IsNotDateOperation[Int, U](Ordering[Int])
          case LongType => new IsNotDateOperation[Long,U](Ordering[Long])
          case ShortType=> new IsNotDateOperation[Short,U](Ordering[Short])
          case FloatType => new IsNotDateOperation[Float,U](Ordering[Float])
          case DoubleType => new IsNotDateOperation[Double,U](Ordering[Double])
          case x: DecimalType => new IsNotDateOperation[Any, U](new DecimalOrdering(x))
          case _ => new IsNotDateOperation[String, U](Ordering[String])
        }
        isNotDateOperation.operation
      case s if s matches "(?i)is timestamp" =>
        val isDateOperation = fieldType match {
          case StringType => new IsTimestampOperation[String, U](Ordering[String])
          case BooleanType => new IsTimestampOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new IsTimestampOperation[Int, U](Ordering[Int])
          case LongType => new IsTimestampOperation[Long,U](Ordering[Long])
          case ShortType=> new IsTimestampOperation[Short,U](Ordering[Short])
          case FloatType => new IsTimestampOperation[Float,U](Ordering[Float])
          case DoubleType => new IsTimestampOperation[Double,U](Ordering[Double])
          case x: DecimalType => new IsTimestampOperation[Any, U](new DecimalOrdering(x))
          case _ => new IsTimestampOperation[String, U](Ordering[String])
        }
        isDateOperation.operation
      case s if s matches "(?i)is not timestamp" =>
        val isNotTimestampOperation = fieldType match {
          case StringType => new IsNotTimestampOperation[String, U](Ordering[String])
          case BooleanType => new IsNotTimestampOperation[Boolean, U](Ordering[Boolean])
          case IntegerType => new IsNotTimestampOperation[Int, U](Ordering[Int])
          case LongType => new IsNotTimestampOperation[Long,U](Ordering[Long])
          case ShortType=> new IsNotTimestampOperation[Short,U](Ordering[Short])
          case FloatType => new IsNotTimestampOperation[Float,U](Ordering[Float])
          case DoubleType => new IsNotTimestampOperation[Double,U](Ordering[Double])
          case x: DecimalType => new IsNotTimestampOperation[Any, U](new DecimalOrdering(x))
          case _ => new IsNotTimestampOperation[String, U](Ordering[String])
        }
        isNotTimestampOperation.operation
    }
  }

  private class DecimalOrdering(x: DecimalType) extends Ordering[Any] {
    import org.apache.spark.sql.BigDecimalHelper

    def compare(a: Any, b: Any) = {
      BigDecimalHelper.convertToDecimal(a)(x).compare(BigDecimalHelper.convertToDecimal(b)(x))
    }
  }

}
