type User record  {
 readonly int id;
 string name;
 Post[] posts;
 Profile? profile?;
};

type Profile record  {
 readonly int id;
 string name;
 int userId = -1;
 User user;
};

type Post record  {
 readonly int id;
 string name;
 User author?;
 int authorId = -1;
 Category[] categories;
 User[]|Profile[] users;
};

type Category record  {
 readonly int id;
 Post[] posts?;
};
