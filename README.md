3levelmemcache
==============

All modern server side applications use memcached as fast caching technology. Memcached comes with multiple languages clients including java. The client provides you an ability to gain access to the memcached servers and perform operations you need. All these operations a low level and require you to have control over synchronous/asynchronous execution of your request, require you to specify serializer to use, require you to manage data at low level. One more challenge any app faces with memcached. Memcached is in memory storage, if server crashes - data is lost. To avoid this kind of issues we write/read data from 2 identical memcached storages. Even if one crashes the other still able to server traffic and application do not see the problem. No outage for application.

Data.com has built own caching framework on top of provided memcached low level client. The distinguishing part of this framework:

it allows you to operate data at higher level. No worries about serialization, about sync/async execution.
it allows you to write into 2 memcached storages
it adds local or JVM cache on top of memcached storages. That gives a huge boost in performance in some cases.
