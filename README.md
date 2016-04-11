Bookmark-plugin-for-eclipse
===========================

## Contributor:

* hanks zhouhan315@gmail.com
* Aloys jiangxinnju@163.com

A simple treeview version bookmark plugin for eclipse

**use github to host my bookmark plugin for online eclipse marketplace**

## What
It is a simple bookmark tools to manage your favorite edited files together.

## Why
We know that during a job in a big project, you only need to modify a few related source files, so bookmark feature in eclipse can do your favor, but the original one
is a tableview, that can not manage more and more bookmarks by folder, so for
the folder, the plugin is created....

## Demo
![alt text][demo]

[demo]: https://raw.githubusercontent.com/hanks/Bookmark-plugin-for-eclipse/master/resources/demo.gif "demo"

## Install
There are two ways to install this plugin.

<ol>
  <li>From local</li>
    <ol>
      <li>Download and *.jar file in release folder to your eclipse plugin folder, and restart eclipse.</li>
      <li>Then select from Window->Show View->Other... to search Bookmark View</li>
    </ol>
  <li>From update site
    <ol>
      <li>Select Help->Install New Software...</li>
      <li>Input <a>https://raw.githubusercontent.com/hanks/Bookmark-plugin-for-eclipse/master/release/update_site/</a> and press Add to install.
  </li>
    </ol>
  </li>
</ol>

## Todo

* short name label
* drag and drop action
* cloud store? This is too big
* i18n
* Open the bookmark located in the history position.
* Add all the open files into bookmark at once.
* Open all the files under the folder.
* 存入非folder/file信息，比如包，断点等
* 文件名支持中文，空格等字符
* 支持folder右键展开所有子folder
* 删除toolbar中无用图标
* 增加排序功能，比如按照时间排序，按照字母序排序
* 每个书签后面添加区域数目，可以保存一个文件的多个区域
* 优化图标，保持和package explore一致
* 同一个folder不允许出现同名文件
* 增加注释功能，允许加入文字说明，鼠标悬停时显示注释
* 导入导出功能
* 搜索，过滤功能
* 修改view视图名称


## Contribution
**Waiting for your pull request**

## Lisence
MIT Lisence
