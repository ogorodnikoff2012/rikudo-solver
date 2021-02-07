package fr.polytechnique.rikudo.hexagonal;

import java.awt.geom.Point2D;
import java.util.Objects;

public class EisensteinInteger {

  public static final EisensteinInteger ZERO = new EisensteinInteger(0, 0);
  public static final EisensteinInteger ONE = new EisensteinInteger(1, 0);
  public static final EisensteinInteger OMEGA = new EisensteinInteger(0, 1);

  private final long a;
  private final long b;

  private static final double OMEGA_REAL_PART = -0.5;
  private static final double OMEGA_IMAG_PART = 0.5 * Math.sqrt(3);

  @Override
  public EisensteinInteger clone() {
    return new EisensteinInteger(a, b);
  }


  public EisensteinInteger(long a, long b) {
    this.a = a;
    this.b = b;
  }

  public double real() {
    return a + b * OMEGA_REAL_PART;
  }

  public double imag() {
    return b * OMEGA_IMAG_PART;
  }

  public long absSqr() {
    return a * a + b * b - a * b;
  }

  public double abs() {
    return Math.sqrt(absSqr());
  }

  EisensteinInteger add(EisensteinInteger rhs) {
    return new EisensteinInteger(a + rhs.a, b + rhs.b);
  }

  EisensteinInteger sub(EisensteinInteger rhs) {
    return new EisensteinInteger(a - rhs.a, b - rhs.b);
  }

  EisensteinInteger neg() {
    return new EisensteinInteger(-a, -b);
  }

  EisensteinInteger conj() {
    return new EisensteinInteger(a - b, -b);
  }

  EisensteinInteger mul(EisensteinInteger rhs) {
    return new EisensteinInteger(a * rhs.a - b * rhs.b, b * rhs.a + a * rhs.b - b * rhs.b);
  }

  public static void main(String[] args) {
    EisensteinInteger x = ONE.add(OMEGA);
    EisensteinInteger xPow = ONE;

    for (int i = 1; i <= 6; ++i) {
      xPow = xPow.mul(x);
      System.out.printf("x ** %d = (%.6f, %.6f)\n", i, xPow.real(), xPow.imag());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EisensteinInteger that = (EisensteinInteger) o;
    return a == that.a &&
        b == that.b;
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b);
  }

  @Override
  public String toString() {
    return "EisensteinInteger{" +
        "a=" + a +
        ", b=" + b +
        '}';
  }
}
