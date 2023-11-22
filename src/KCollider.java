import processing.core.PApplet;
import processing.core.PVector;

import static java.lang.Math.*;

/* handles 2d collisions for points, circles, and polygons */
@SuppressWarnings("unused") // keeps my IDE happy
public class KCollider {
  /* collision detection interface - this really just defers to one of *many* collision detection functions.
   * It returns true if the hitboxes intersect, and false if they don't. If they do collide, the translation
   * vector is stored in transVec (trans rights!) if it isn't null (unless the collision involves a point or
   * a line, in which case transVec is always (0, 0)). */
  public static boolean colliding(Hitbox h1, Hitbox h2, PVector transVec) {
    // this is messy as hell, but it's the best way I can think of to do it :/
    Shape s1 = h1.shape, s2 = h2.shape;
    if (s1 == Shape.POINT && s2 == Shape.POINT) {
      return pointEqualsPoint(h1, h2, transVec);
    }
    else if (s1 == Shape.POINT && s2 == Shape.CIRCLE) {
      return pointInCircle(h1, h2, transVec);
    }
    else if (s1 == Shape.POINT && s2 == Shape.POLYGON) {
      return pointInPolygon(h1, h2, transVec);
    }
    else if (s1 == Shape.POINT && s2 == Shape.LINE) {
      return pointOnLine(h1, h2, transVec);
    }
    else if (s1 == Shape.CIRCLE && s2 == Shape.POINT) {
      return pointInCircle(h2, h1, transVec);
    }
    else if (s1 == Shape.CIRCLE && s2 == Shape.CIRCLE) {
      return circleToCircleCollide(h1, h2, transVec);
    }
    else if (s1 == Shape.CIRCLE && s2 == Shape.POLYGON) {
      return circleToPolygonCollide(h1, h2, transVec, false);
    }
    else if (s1 == Shape.CIRCLE && s2 == Shape.LINE) {
      if(transVec != null) transVec.set(0, 0);
      return lineInCircle(h2.start, h2.end, h1.position, h1.radius, new PVector());
    }
    else if (s1 == Shape.POLYGON && s2 == Shape.POINT) {
      return pointInPolygon(h2, h1, transVec);
    }
    else if (s1 == Shape.POLYGON && s2 == Shape.CIRCLE) {
      return circleToPolygonCollide(h2, h1, transVec, true);
    }
    else if (s1 == Shape.POLYGON && s2 == Shape.POLYGON) {
      return polygonToPolygonCollide(h1, h2, transVec);
    }
    else if (s1 == Shape.POLYGON && s2 == Shape.LINE) {
      return lineInPolygon(h2, h1, transVec);
    }
    else if (s1 == Shape.LINE && s2 == Shape.POINT) {
      return pointOnLine(h2, h1, transVec);
    }
    else if (s1 == Shape.LINE && s2 == Shape.CIRCLE) {
      if(transVec != null) transVec.set(0, 0);
      return lineInCircle(h1.start, h1.end, h2.position, h2.radius, new PVector());
    }
    else if (s1 == Shape.LINE && s2 == Shape.POLYGON) {
      return lineInPolygon(h1, h2, transVec);
    }
    else {
      return lineIntersection(h1, h2, transVec);
    }
  }
  // overload to make transVec optional
  public static boolean colliding(Hitbox h1, Hitbox h2) {
    return colliding(h1, h2, null);
  }

  /* fancy collision detection algorithms */
  /** Determines if two polygons are overlapping. This implementation uses the "Separating Axis Theorem", which
   * is really just an overly fancy way of saying "If you can draw a line between two things, they don't overlap."
   * This version also returns the "Minimum Translation Vector", which is how much (and what direction) to move
   * the first polygon so that it no longer overlaps with the second one. **/
  public static boolean polygonToPolygonCollide(Hitbox poly1, Hitbox poly2, PVector transVec) {
    // SAT is really fast relative to other collision algorithms, but "really fast" is still
    // pretty slow in this context. Bounding box checks are *actually* really fast and will
    // rule out a lot of polygons that definitely don't overlap with this one.
    if (!poly1.bbox.rectIntersection(poly2.bbox)) return false;

    // find the edges of both polygons and merge them into a single array
    PVector[] poly1Edges = getEdges(poly1);
    PVector[] poly2Edges = getEdges(poly2);
    PVector[] allEdges = new PVector[poly1Edges.length + poly2Edges.length];
    System.arraycopy(poly1Edges, 0, allEdges, 0, poly1Edges.length);
    System.arraycopy(poly2Edges, 0, allEdges, poly1Edges.length, poly2Edges.length);

    // used for constructing the MTV - the "minimum translation vector", not the tv station
    float mtvLength = Float.POSITIVE_INFINITY;
    PVector mtvAxis = new PVector(0, 0);

    // build all axes (axes? axises?) and project both polygons onto them
    for (PVector edge : allEdges) {
      float edgeLength = edge.mag();

      // create an axis that is perpendicular to the edge, and normalized (it has a length of 1)
      PVector axis = new PVector(
          -edge.y / edgeLength,
          edge.x / edgeLength
      );

      // project both polygons onto the axis
      float[] thisProj = projectOntoAxis(poly1, axis);
      float[] otherProj = projectOntoAxis(poly2, axis);

      // polygons are only overlapping if *all* their projections overlap, so we can immediately
      // return if we find a projection where they don't overlap (this is why SAT is so fast)
      float overlap = intervalDistance(thisProj, otherProj);
      if (overlap > 0) return false;
      else {
        // update the MTV if this is the smallest overlap found so far
        if (abs(overlap) < mtvLength) {
          mtvLength = abs(overlap);
          if (thisProj[0] < otherProj[0]) mtvAxis.set(-axis.x, -axis.y);
          else mtvAxis.set(axis);
        }
      }
    }

    // set transVec to the mtv if it isn't null
    if (transVec != null) transVec.set(PVector.mult(mtvAxis, mtvLength));
    return true;
  }

  // determines if a polygon and a circle overlap
  public static boolean circleToPolygonCollide(Hitbox circle, Hitbox poly, PVector transVec, boolean invert) {
    // bounding box checks are very fast and prevent a lot of unnecessary checks
    if (!circle.bbox.rectIntersection(poly.bbox)) return false;

    // check if any of the edges on the polygon intersect the circle, and find the closest intersection point
    PVector closest = new PVector();
    float closestDistance = Float.POSITIVE_INFINITY;
    // do some black magic in the for loop constructor to use the first and last points on the first iteration
    for (int i = 0, j = poly.points.length - 1; i < poly.points.length; j = i++) {
      PVector p1 = poly.points[i], p2 = poly.points[j];

      // return true if the edge intersects with the circle, and the intersection point is the closest one found
      PVector p = new PVector();
      if (lineInCircle(p1, p2, circle.position, circle.radius, p)) {
        float d = PVector.sub(p, circle.position).mag();
        if (d < closestDistance) {
          closestDistance = d;
          closest.set(p);
        }
      }
    }

    if (closestDistance < Float.POSITIVE_INFINITY) {
      // find a translation vector - if invert is true, the vector moves the polygon out of the circle, otherwise
      // it moves the circle out of the polygon
      if (transVec != null) {
        PVector delta = PVector.sub(closest, circle.position);
        float moveDistance = circle.radius - delta.mag();
        delta.setMag(-moveDistance);

        // invert the vector if needed so that the circle is always moved outside the polygon
        if (pointInPolygon(new Hitbox(PVector.add(circle.position, delta)), poly, null)) invert = !invert;

        transVec.set(invert ? PVector.mult(delta, -1) : delta);
      }
      return true;
    }
    return false;
  }

  // determines if two circles overlap
  public static boolean circleToCircleCollide(Hitbox c1, Hitbox c2, PVector transVec) {
    PVector distanceVector = new PVector(c1.position.x - c2.position.x,
                                         c1.position.y - c2.position.y);

    if (distanceVector.magSq() < pow(c1.radius + c2.radius, 2)) {
      // find a translation vector to move the first circle out of the second
      if (transVec != null) {
        PVector vec = new PVector(distanceVector.x, distanceVector.y);
        vec.setMag(c1.radius + c2.radius);
        vec.sub(distanceVector);
        transVec.set(vec);
      }
      return true;
    }
    else {
      return false;
    }
  }

  /** Returns whether a point is inside the polygon. I don't fully understand how
   * this specific implementation works (I just grabbed it off of StackOverflow),
   * but the short explanation is that it creates a line that starts at the point
   * and extends to infinity, then counts how many times it crosses an edge of
   * the polygon. If that number is odd, the point is inside the polygon. **/
  public static boolean pointInPolygon(Hitbox point, Hitbox polygon, PVector transVec) {
    // ray-casting algorithm based on
    // https://wrf.ecse.rpi.edu/Research/Short_Notes/pnpoly.html
    boolean inside = false;
    for (int i = 0, j = polygon.points.length - 1; i < polygon.points.length; j = i++) {
      float xi = polygon.points[i].x, yi = polygon.points[i].y;
      float xj = polygon.points[j].x, yj = polygon.points[j].y ;

      if (((yi > point.position.y) != (yj > point.position.y)) &&
          (point.position.x < (xj - xi) * (point.position.y - yi) / (yj - yi) + xi)) {
        inside = !inside;
      }
    }

    if (transVec != null) transVec.set(0, 0);
    return inside;
  }

  // returns whether a point is inside a circle
  public static boolean pointInCircle(Hitbox point, Hitbox circle, PVector transVec) {
    if (transVec != null) transVec.set(0, 0);
    return PVector.sub(point.position, circle.position).magSq() < circle.radiusSquared;
  }

  // returns whether two points are at the same integer coordinates - this is really only here for completeness and
  // will probably never be used
  public static boolean pointEqualsPoint(Hitbox p1, Hitbox p2, PVector transVec) {
    if (transVec != null) transVec.set(0, 0);
    return (int)p1.position.x == (int)p2.position.x && (int)p1.position.y == (int)p2.position.y;
  }

  /* unified class that handles all collisions and can be any shape */
  public static class Hitbox {
    public final Shape shape;
    public PVector position, start, end;
    public float radius, radiusSquared;
    public BoundingRect bbox, absoluteBBox;
    public PVector[] points, absolutePoints;

    // point ctor
    Hitbox(float x, float y) {
      shape = Shape.POINT;
      position = new PVector(x, y);
    }

    // circle ctor
    Hitbox(float x, float y, float r) {
      shape = Shape.CIRCLE;
      position = new PVector(x, y);
      radius = r;
      radiusSquared = r * r; // used to speed up some collision checks
      bbox = new BoundingRect(position.x - radius, position.y - radius, radius * 2, radius * 2);
    }

    // polygon ctors
    Hitbox(float[][] pts) throws IllegalArgumentException {
      shape = Shape.POLYGON;
      init(pts);
    }

    // overload that sets the position in the constructor
    Hitbox(float[][] pts, float x, float y) throws IllegalArgumentException {
      this(pts);
      setPos(x, y);
    }

    // rect ctor that's actually just a wrapper for the polygon ctor
    Hitbox(float x, float y, float w, float h) {
      this(new float[][]{{0, 0}, {w, 0}, {w, h}, {0, h}}, x, y);
    }

    // line ctor - this requires PVectors because the rect ctor overlaps it
    Hitbox(PVector start, PVector end) {
      shape = Shape.LINE;
      this.start = start;
      this.end = end;
    }

    // overloads that take PVectors
    Hitbox(PVector pos) {
      this(pos.x, pos.y);
    }
    Hitbox(PVector pos, float r) {
      this(pos.x, pos.y, r);
    }
    Hitbox(PVector[] pts) throws IllegalArgumentException {
      shape = Shape.POLYGON;
      float[][] floatPoints = new float[pts.length][];
      for (int i = 0; i < pts.length; ++i) {
        floatPoints[i] = new float[]{pts[i].x, pts[i].y};
      }
      init(floatPoints);
    }
    Hitbox(PVector[] pts, float x, float y) throws IllegalArgumentException {
      this(pts);
      setPos(x, y);
    }
    Hitbox(float[][] pts, PVector pos) throws IllegalArgumentException {
      this(pts);
      setPos(pos);
    }
    Hitbox(PVector[] pts, PVector pos) throws IllegalArgumentException {
      this(pts);
      setPos(pos);
    }

    // sets the hitbox's position relative to the origin - if the hitbox is a line, the start point (the first point
    // in to the ctor) will be the moved to the new position
    public void setPos(float x, float y) {
      switch (shape) {
        case POINT:
          position.set(x, y);
          break;
        case CIRCLE:
          position.set(x, y);
          bbox.x = x - radius;
          bbox.y = y - radius;
          break;
        case POLYGON:
          for (int i = 0; i < absolutePoints.length; ++i) {
            points[i].set(absolutePoints[i].x + x, absolutePoints[i].y + y);
          }

          // also move the bounding box
          bbox.x = absoluteBBox.x + x;
          bbox.y = absoluteBBox.y + y;
          break;
        case LINE:
          PVector delta = PVector.sub(end, start);
          start.set(x, y);
          end.set(PVector.add(start, delta));
      }
    }
    public void setPos(PVector vec) {
      setPos(vec.x, vec.y);
    }

    // moves the hitbox relative to its current position
    public void modPos(float x, float y) {
      switch (shape) {
        case POINT:
          position.x += x;
          position.y += y;
          break;
        case CIRCLE:
          position.x += x;
          position.y += y;
          bbox.x += x;
          bbox.y += y;
          break;
        case POLYGON:
          for (PVector p : points) {
            p.x += x;
            p.y += y;
          }

          // also move the bounding box
          bbox.x += x;
          bbox.y += y;
          break;
        case LINE:
          start.x += x;
          start.y += y;
          end.x += x;
          end.y += y;
      }
    }
    public void modPos(PVector vec) {
      modPos(vec.x, vec.y);
    }

    // used to overload the polygon ctor
    private void init(float[][] pts) throws IllegalArgumentException {
      // throw an exception if the polygon has less than three points
      if (pts.length < 3) {
        throw new IllegalArgumentException("Polygons must have at least three points!");
      }

      absolutePoints = new PVector[pts.length];
      for (int i = 0; i < pts.length; ++i) {
        absolutePoints[i] = new PVector(pts[i][0], pts[i][1]);
      }
      points = new PVector[absolutePoints.length];
      for (int i = 0; i < absolutePoints.length; ++i) {
        points[i] = new PVector(absolutePoints[i].x, absolutePoints[i].y);
      }

      absoluteBBox = KCollider.findBBox(this);
      bbox = new BoundingRect(absoluteBBox);
    }
  }
  // a bounding rectangle, used to do polygon and circle collisions
  public static class BoundingRect {
    public float x, y, w, h;

    // ctor
    BoundingRect(float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }

    // copy ctor
    BoundingRect(BoundingRect other) {
      this(other.x, other.y, other.w, other.h);
    }

    // checks if a point is inside the rectangle
    public boolean pointIntersection(float px, float py) {
      return px > x && px < x + w && py > y && py < y + h;
    }
    // overload that takes a PVector
    public boolean pointIntersection(PVector vec) {
      return pointIntersection(vec.x, vec.y);
    }

    // checks if the rectangle overlaps with another rectangle using math i flagrantly stole from StackOverflow :)
    public boolean rectIntersection(float rx, float ry, float rw, float rh) {
      return x < rx + rw && rx < x + w && y < ry + rh && ry < y + h;
    }
    // overload that takes another BoundingRect
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean rectIntersection(BoundingRect other) {
      return rectIntersection(other.x, other.y, other.w, other.h);
    }
  }

  /* misc. functions */
  // finds the bounding box of a polygon collider - this is the smallest non-rotated box that it will fit inside
  public static BoundingRect findBBox(Hitbox poly) {
    PVector minPoint = new PVector(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    PVector maxPoint = new PVector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

    for (PVector p : poly.absolutePoints) {
      if (p.x < minPoint.x) minPoint.x = p.x;
      else if (p.x > maxPoint.x) maxPoint.x = p.x;

      if (p.y < minPoint.y) minPoint.y = p.y;
      else if (p.y > maxPoint.y) maxPoint.y = p.y;
    }

    return new BoundingRect(minPoint.x, minPoint.y, maxPoint.x - minPoint.x, maxPoint.y - minPoint.y);
  }

  // converts the vertices of a polygon into edge vectors, used for SAT and circle collision
  public static PVector[] getEdges(Hitbox poly) {
    PVector[] edges = new PVector[poly.points.length];
    // do some black magic in the for loop constructor to use the last point and the first point for the first edge
    for (int i = 0, j = poly.points.length - 1; i < poly.points.length; j = i++) {
      PVector pi = poly.points[i];
      PVector pj = poly.points[j];
      edges[i] = new PVector(pi.x - pj.x, pi.y - pj.y);
    }
    return edges;
  }

  // returns the distance/gap between two intervals - if this is < 0, the intervals overlap
  public static float intervalDistance(float[] i1, float[] i2) {
    if (i1[0] < i2[0]) { return i2[0] - i1[1]; }
    else { return i1[0] - i2[1]; }
  }

  // projects a polygon onto an axis and returns the interval it creates on that axis - think of this as "squashing"
  // the polygon onto a line, then returning the area it covers
  public static float[] projectOntoAxis(Hitbox poly, PVector axis) {
    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;
    // precalculate this to save performance
    float axisLength = axis.mag();

    // find the minimum and maximum points in the projection - these are the two endpoints
    // of the squashed line
    for (PVector point : poly.points) {
      float projection = point.dot(axis) / axisLength;
      if (projection < min) {min = projection;}
      if (projection > max) {max = projection;}
    }

    return new float[]{min, max};
  }

  // returns whether or not a line segment intersects a circle
  public static boolean lineInCircle(PVector a, PVector b, PVector cPos, float r, PVector closest) {
    // find the closest point on the line segment to the circle using some very fancy math
    PVector atp = PVector.sub(cPos, a);
    PVector atb = PVector.sub(b, a);
    float t = PApplet.constrain(PVector.dot(atp, atb) / atb.magSq(), 0, 1);

    closest.set(
        a.x + atb.x * t,
        a.y + atb.y * t
    );

    // if the point is within the circle, the line intersects the circle
    return PVector.sub(closest, cPos).magSq() <= pow(r, 2);
  }

  // returns whether or not a line intersects a polygon
  public static boolean lineInPolygon(Hitbox line, Hitbox poly, PVector transVec) {
    if (transVec != null) transVec.set(0, 0);

    // loop through each edge of the polygon and check if the line intersects one of them
    for (int i = 0, j = poly.points.length - 1; i < poly.points.length; j = i++) {
      if (lineIntersection(poly.points[i], poly.points[j], line.start, line.end)) return true;
    }
    return false;
  }

  // returns whether or not two lines intersect
  public static boolean lineIntersection(PVector p0, PVector p1, PVector p2, PVector p3) {
    PVector d1 = PVector.sub(p1, p0);
    PVector d2 = PVector.sub(p3, p2);

    float s = (-d1.y * (p0.x - p2.x) + d1.x * (p0.y - p2.y)) / (-d2.x * d1.y + d1.x * d2.y);
    float t = ( d2.x * (p0.y - p2.y) - d2.y * (p0.x - p2.x)) / (-d2.x * d1.y + d1.x * d2.y);

    return (s >= 0 && s <= 1 && t >= 0 && t <= 1);
  }
  // overload that takes hitboxes and a translation vector
  public static boolean lineIntersection(Hitbox l1, Hitbox l2, PVector transVec) {
    if (transVec != null) transVec.set(0, 0);
    return lineIntersection(l1.start, l1.end, l2.start, l2.end);
  }

  // returns whether or not a point is on a line
  public static boolean pointOnLine(Hitbox point, Hitbox line, PVector transVec) {
    float d1 = PVector.dist(line.start, point.position);
    float d2 = PVector.dist(line.end, point.position);
    if (transVec != null) transVec.set(0, 0);
    return d1 + d2 == PVector.dist(line.start, line.end);
  }

  // collider shapes
  public enum Shape {
    POINT,
    CIRCLE,
    POLYGON,
    LINE
  }
}