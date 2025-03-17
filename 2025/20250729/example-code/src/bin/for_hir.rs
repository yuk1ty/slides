fn main() {
    let nums = vec![1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    let mut ans = 0;
    for num in nums {
        ans += num;
    }
    assert_eq!(ans, 55);
}
