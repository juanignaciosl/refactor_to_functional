# Refactor to Functional

In this workshop we'll begin from a "classic", OO-ish Scala web application (aka: "Java written in Scala") and we'll
refactor it to a functional approach. In the way, we'll learn several FP patterns and enjoy composition.

## What this won't cover

- Implementation of the patterns, no time for that. We'll mostly use [Cats][5] implementations.
- Math accuracy, obscure definitions or mentions to Category Theory.

## The Path to Enlightenment

### Type classes

[[TAGS]][3] A type class is a parameterised trait representing some sort of general functionality that we would like
to apply to a wide range of types.

[[SC]][4] The instances of a type class provide implementations for the types we care about. (...) A type class
interface is any functionality we expose to users.

[[ES]][7] The type class pattern separates the implementation of functionality (the type class
instance (...)) from the type the functionality is provided for (...).
This is the basic pattern for type classes.

A type class is like a trait, defining an interface. However, with type classes we can:
- Plugin different implementations of an interface for a given class
- implement an interface without modifying existingcode

#### Use cases

- Sorting.
- Encoders (CSV, JSON...).
- Type-safe equality.

### Semigroup

[[SC]][4] A semigroup for a type A is an algebra consisting in the following:

- Some type `A`.
- An associative binary operation, `op`, that takes two values of type `A` and combines them into one:
 `op(op(x,y), z) == op(x, op(y,z))` for any choice of `x: A, y: A, z: A`.

#### Use cases

- [[SC]][4] Nonempty list .

### Monoid

[[FPS]][1] A monoid for a type A is an algebra consisting in the following:

- Some type `A`.
- An associative binary operation, `op`, that takes two values of type `A` and combines them into one:
 `op(op(x,y), z) == op(x, op(y,z))` for any choice of `x: A, y: A, z: A`.
- A value, `zero: A`, that is an identity for that operation: `op(x, zero) == x` and `op(zero, x) == x` for any `x: A`.

Monoids are semigroups.

#### Use cases

- String concatenation.
- Integer addition.
- [[FPS]][1] Parallel processing over a collection.
- [[SC]][4] Generic sum.
- [[SC]][4] Commutative replicated data types (CRDTs).
- Map-reduce.

##### Composing

- [[FPS]][1] Merging key-value `Map`s as long as value is itself a monoid.

### Functor

[[FPS]][4] "A data type that implements map”. [[SC]][4] We typically first encounter map when iterang over List but
rather than traversing the list, we should think of it as transforming all of the values inside in one go.

[[SC]][4] Formally, a functor is a type `F[A]` with an operation `map` with type `(A => B) => F[B]`. (...) Functors
represent sequencing behaviours

#### Use cases

- `List`, `Option`...
- Generic `unzip`.

### Applicative functors

Less powerful than monads, but more general.

#### Use cases

- (Potentially) infinite streams.
- Validation.
- Testing asynchronous code.

### Monads

[[SC]][4] "Anything with a constructor and a flatMap method". (...) A mechanism for sequencing computations.

Monads are functors.

#### Use cases

- _Identity monad_.
- State.
- Reader.
- IO.

### Free Monad

[FRDM][3]: model domain behaviors as pure data and then supply interpreters that work on that data in specific contexts.
Features:

- Behaviors represented as pure data forming a closed algebraic data type.
- Strict separation between creation of the computation and its execution.
- Execution of computation comes in the form of interpreters, and you can have multiple interpreters for the same structure.

### Monad transformers

Do monads _compose_?

#### ReaderT / Kleisli

## References

### Libraries

- [Cats][5]
- [Shapeless][6]

### Books

- [[FPS]][1]: Functional Programming in Scala
- [[FRDM]][2]: Functional and Reactive Domain Modeling
- [[TAGS]][3]: The Type Astronaut's Guide to Shapeless
- [[SC]][4]: Scala with Cats
- [[ES]][7]: Essential Scala

[1]: https://www.manning.com/books/functional-programming-in-scala
[2]: https://www.manning.com/books/functional-and-reactive-domain-modeling
[3]: https://underscore.io/training/courses/advanced-shapeless/
[4]: https://underscore.io/training/courses/advanced-scala/
[5]: https://typelevel.org/cats/
[6]: https://github.com/milessabin/shapeless
[7]: https://underscore.io/books/essential-scala/
