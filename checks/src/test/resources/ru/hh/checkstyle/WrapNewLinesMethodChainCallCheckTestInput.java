package ru.hh.checkstyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WrapNewLinesMethodChainCallCheckTestInput {

  // Class variable declaration - OK
  private static final List<Integer> myAbsolutelyVeryLongNamedIntegerList = List.of(1, 2, 3);

  // Class variable declaration - NOT OK
  private static final Set<String> myAbsolutelyVeryLongNamedStringStaticSet = myAbsolutelyVeryLongNamedIntegerList
      .stream().map(integer -> integer * integer).filter(integer -> integer % 2 == 0).map(String::valueOf).findAny()
      .stream().collect(Collectors.toSet());
  // Class variable declaration - OK
  private static final Set<String> myAbsolutelyVeryLongNamedStringStaticSet2 = myAbsolutelyVeryLongNamedIntegerList
      .stream()
      .map(integer -> integer * integer)
      .filter(integer -> integer % 2 == 0)
      .map(String::valueOf)
      .findAny()
      .stream()
      .collect(Collectors.toSet());

  public static void main(String[] args) {
    // method variable declaration - NOT OK
    Set<String> myAbsolutelyVeryLongNamedStringSet = myAbsolutelyVeryLongNamedIntegerList
        .stream().map(integer -> integer * integer).filter(integer -> integer % 2 == 0).map(String::valueOf).findAny()
        .stream().collect(Collectors.toSet());
    // method variable declaration - OK
    Set<String> myAbsolutelyVeryLongNamedStringSet2 = myAbsolutelyVeryLongNamedIntegerList
        .stream()
        .map(integer -> integer * integer)
        .filter(integer -> integer % 2 == 0)
        .map(String::valueOf)
        .findAny()
        .stream()
        .collect(Collectors.toSet());

    //expression - OK
    myAbsolutelyVeryLongNamedIntegerList.stream().filter(i -> i == 1).map(String::valueOf).toList().forEach(System.out::println);

    //expression - NOT OK
    myAbsolutelyVeryLongNamedIntegerList
        .stream().map(integer -> integer * integer).filter(integer -> integer % 2 == 0).map(String::valueOf).findAny()
        .stream().collect(Collectors.toSet());
    //expression - OK
    myAbsolutelyVeryLongNamedIntegerList
        .stream()
        .map(integer -> integer * integer)
        .filter(integer -> integer % 2 == 0)
        .map(String::valueOf)
        .findAny()
        .stream()
        .collect(Collectors.toSet());

    //expression with static method - NOT OK
    List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        .stream().filter(myVeryLongNameForJustASimpleInteger -> myVeryLongNameForJustASimpleInteger % 2 == 0)
        .toList();
    //expression with static method - OK
    List
        .of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        .stream()
        .filter(myVeryLongNameForJustASimpleInteger -> myVeryLongNameForJustASimpleInteger % 2 == 0)
        .toList();

    //expression with new literal - NOT OK
    new ArrayList<Integer>().stream()
        .filter(myVeryLongNameForJustASimpleInteger -> myVeryLongNameForJustASimpleInteger % 2 == 0)
        .toList().stream().map(String::valueOf)
        .collect(Collectors.toSet());
    //expression with new literal - OK
    new ArrayList<Integer>()
        .stream()
        .filter(myVeryLongNameForJustASimpleInteger -> myVeryLongNameForJustASimpleInteger % 2 == 0)
        .toList()
        .stream()
        .map(String::valueOf)
        .collect(Collectors.toSet());

    // expression inside method call - OK
    myMethod(myAbsolutelyVeryLongNamedIntegerList.stream().collect(Collectors.toSet()), 100, 200);
    // expression inside method call - NOT OK
    myMethod(myAbsolutelyVeryLongNamedIntegerList.stream().map(theLongestIntNameEver -> theLongestIntNameEver + theLongestIntNameEver)
        .filter(theLongestIntNameEver -> theLongestIntNameEver == 1).collect(Collectors.toSet()), 100, 200);
    // expression inside method call - OK
    myMethod(
        myAbsolutelyVeryLongNamedIntegerList
            .stream()
            .map(theLongestIntNameEver -> theLongestIntNameEver + theLongestIntNameEver)
            .filter(theLongestIntNameEver -> theLongestIntNameEver == 1)
            .collect(Collectors.toSet()),
        100,
        200
    );
    // expression inside method call - OK
    myMethod(
        Stream
            .of(1, 2, 3, 4, 5, 6)
            .map(theLongestIntNameEver -> theLongestIntNameEver + theLongestIntNameEver)
            .filter(theLongestIntNameEver -> theLongestIntNameEver == 1)
            .collect(Collectors.toSet()),
        100,
        200
    );
  }

  private static void myMethod(Collection<Object> col, int i, int j) {
    //do nothing
  }

  // long lambdas - NOT OK
  boolean myMethod1(int id) {
    return Optional.ofNullable(1).map(integer -> List.of(integer).stream().map(integer -> {
      return integer == 1 ? Boolean.TRUE : Boolean.FALSE;
    }).findAny().orElse(Boolean.FALSE)).orElseGet(() -> {
      return Boolean.FALSE;
    });
  }

  // long lambdas - OK
  boolean myMethod2(int id) {
    return Optional
        .ofNullable(1)
        .map(
            integer ->
                List
                    .of(integer)
                    .stream()
                    .map(integer -> {
                      return integer == 1 ? Boolean.TRUE : Boolean.FALSE;
                    })
                    .findAny()
                    .orElse(Boolean.FALSE))
        .orElseGet(() -> {
          return Boolean.FALSE;
        });
  }

  // not new line before first call - NOT OK
  public Set<String> myMethod3() {
    return Stream.concat(
        myAbsolutelyVeryLongNamedStringStaticSet.stream(),
        myAbsolutelyVeryLongNamedIntegerList()
            .stream()
            .map(integer -> integer * integer)
            .map(String::valueOf)
    ).collect(Collectors.toSet());
  }

  // new line before first call - OK
  public Set<String> myMethod4() {
    return Stream
        .concat(
            myAbsolutelyVeryLongNamedStringStaticSet.stream(),
            myAbsolutelyVeryLongNamedIntegerList()
                .stream()
                .map(integer -> integer * integer)
                .map(String::valueOf)
        )
        .collect(Collectors.toSet());
  }

  // long chain as arg - NOT OK
  public Set<String> myMethod5() {
    return Stream
        .concat(
            myAbsolutelyVeryLongNamedStringStaticSet.stream(),
            myAbsolutelyVeryLongNamedIntegerList().stream().map(integer -> integer * integer)
                .map(String::valueOf).map(Integer::valueOf).map(String::valueOf)
                .map(Integer::valueOf).map(String::valueOf)
                .map(Integer::valueOf).map(String::valueOf)
        )
        .collect(Collectors.toSet());
  }

  // NOT OK
  public Set<String> myMethod6() {
    List<Integer> list = List.of(1);
    return list.isEmpty() ? list.stream().filter(integer -> integer != 2).map(String::valueOf).map(String::strip)
        .filter(String::isBlank).map(String::length).map(String::valueOf).collect(Collectors.toSet()) : list.stream().map(String::valueOf)
        .map(String::length).map(String::valueOf).map(String::length).map(String::valueOf).map(String::length).map(String::valueOf)
        .map(String::length).map(String::valueOf).collect(Collectors.toSet());
  }

  // OK
  public Set<String> myMethod6() {
    List<Integer> list = List.of(1);
    return list.stream().findAny().isEmpty() ? list
        .stream()
        .filter(integer -> integer != 2)
        .map(String::valueOf)
        .map(String::strip)
        .collect(Collectors.toSet()) : list
        .stream()
        .map(String::valueOf)
        .map(String::length)
        .map(String::valueOf)
        .map(String::length)
        .map(String::valueOf)
        .collect(Collectors.toSet());
  }

  // long lambdas - OK
  boolean myMethod7(int id) {
    return Optional
        .ofNullable(1)
        .map(
            integer -> List
                .of(integer)
                .stream()
                .map(i -> i == 1 ? Boolean.TRUE : Boolean.FALSE)
                .filter(b -> b.equals(Boolean.TRUE))
                .findAny()
                .orElse(Boolean.FALSE))
        .orElseGet(() -> {
          return Boolean.FALSE;
        });
  }
}
