/**
 * crypt.io - Encryption enabled browser storage
 *
 * https://www.github.com/jas-/crypt.io
 *
 * Author: Jason Gerfen <jason.gerfen@gmail.com>
 * License: MIT (see LICENSE)
 */
(function(window, undefined) {

  'use strict';

  /**
   * @function cryptio
   * @abstract Namespace for saving/retrieving encrypted HTML5 storage engine
   * data
   */
  var cryptio = cryptio || function() {

    /**
     * @var {Object} defaults
     * @abstract Default set of options for plug-in
     *
     * @param {Boolean} encrypt Optionally encrypt stored data
     * @param {String} passphrase Passphrase to use (optional)
     * @param {String} storage Storage mechanism (local, session or cookies)
     */
    var defaults = {
      passphrase: '',
      storage: 'local'
    };

    /**
     * @method setup
     * @abstract Initial setup routines
     */
    var setup = setup || {

      /**
       * @function set
       * @abstract Initialization
       *
       * @param {Object} opts Plug-in option object
       */
      init: function(opts) {
        opts.passphrase = opts.passphrase || (crypto.key(opts.passphrase) || crypto.key());
      }
    };

    /**
     * @method storage
     * @abstract Interface to handle storage options
     */
    var storage = storage || {

      /**
       * @function quota
       * @abstract Tests specified storage option for current amount of space available.
       *  - Cookies: 4K
       *  - localStorage: 5MB
       *  - sessionStorage: 5MB
       *  - default: 5MB
       *
       * @param {String} t Type of storage specified
       *
       * @returns {Boolean}
       */
      quota: function(storage) {
        var max = /local|session/.test(storage) ? 1024 * 1025 * 5 :
          1024 * 4,
          cur = libs.total(storage),
          total = max - cur;

        return total > 0;
      },

      /**
       * @function remove
       * @abstract Interface for removing a key from storage
       *
       * @param {Object} opts Default options
       * @param {String} key Index of storage object
       *
       * @returns {Boolean}
       */
      remove: function(opts, key) {
        if ((typeof opts.storage === 'string') || !opts.storage) {
          this[opts.storage] ?
            this[opts.storage].remove(key) :
            this.local.remove(key);
        } else {
          opts.storage.remove(key);
        }
        return true;
      },

      /**
       * @function set
       * @abstract Interface for saving to available storage mechanisms
       *
       * @param {Object} opts Default options
       * @param {String} key Index of storage object
       * @param {Object} data Data to be stored
       *
       * @returns {Boolean}
       */
      set: function(opts, key, data) {
        if (!storage.quota(opts.storage)) {
          return false;
        }

        if (data && opts.passphrase) {
          data = sjcl.encrypt(opts.passphrase, storage.fromJSON(data));
        }

        if ((typeof opts.storage === 'string') || !opts.storage) {
          this[opts.storage] ?
            this[opts.storage].set(key, data) :
            this.local.set(key, data);
        } else {
          opts.storage.set(key, data);
        }
        return true;
      },

      /**
       * @function get
       * @abstract Interface for retrieving from available storage mechanisms
       *
       * @param {Object} opts Default options
       * @param {String} key Index of storage object
       *
       * @returns {Object}
       */
      get: function(opts, key) {
        var ret = false;

        if ((typeof opts.storage === 'string') || !opts.storage) {
          ret = this[opts.storage] ?
            this[opts.storage].get(key) :
            this.local.get(key);
        } else {
          ret = opts.storage.get(key);
        }

        if (typeof ret === 'string') {
          ret = sjcl.decrypt(opts.passphrase, ret);
        }

        return storage.toJSON(ret);
      },

      /**
       * @function fromJSON
       * @abstract Convert to JSON object to string
       *
       * @param {Object|Array|String} obj Object, Array or String to convert to JSON object
       *
       * @returns {String}
       */
      fromJSON: function(obj) {
        var ret;
        try {
          ret = JSON.stringify(obj);
        } catch (e) {
          ret = obj;
        }
        return ret;
      },

      /**
       * @function toJSON
       * @abstract Creates JSON object from formatted string
       *
       * @param {String} obj Object to convert to JSON object
       *
       * @returns {Object}
       */
      toJSON: function(obj) {
        var ret;
        try {
          ret = JSON.parse(obj);
        } catch (e) {
          ret = obj;
        }
        return ret;
      },

      /**
       * @method cookie
       * @abstract Method for handling setting & retrieving of cookie objects
       */
      cookie: {

        /**
         * @function set
         * @abstract Handle setting of cookie objects
         *
         * @param {String} key Key to use for cookies
         * @param {String|Object} value String or object to place in cookie
         *
         * @returns {Boolean}
         */
        set: function(key, value) {
          var date = new Date();
          date.setTime(date.getTime() + (30 * 24 * 60 * 60 * 1000));
          document.cookie = key + '=' + value + ';expires=' + date.toGMTString() +
            ';path=/;domain=' + this.domain();
          return true;
        },

        /**
         * @function get
         * @abstract Handle retrieval of cookie objects
         *
         * @param {String} key cookie key
         *
         * @returns {String|False}
         */
        get: function(key) {
          var i, index, value, content = document.cookie.split(";");
          for (i = 0; i < content.length; i++) {
            index = content[i].substr(0, content[i].indexOf('='));
            value = content[i].substr(content[i].indexOf('=') + 1);
            index = index.replace(/^\s+|\s+$/g, '');
            if (index == key) {
              return unescape(value);
            }
          }
          return false;
        },

        /**
         * @function domain
         * @abstract Provides current domain of client for cookie realm
         *
         * @returns {String}
         */
        domain: function() {
          return location.hostname;
        }
      },

      /**
       * @method local
       * @abstract Method for handling setting & retrieving of localStorage objects
       */
      local: {

        /**
         * @function set
         * @abstract Handle setting & retrieving of localStorage objects
         *
         * @param {String} key Index of storage object
         * @param {Object} data Data to be stored
         *
         */
        set: function(key, data) {
          window.localStorage.setItem(key, data);
        },

        /**
         * @function get
         * @abstract Handle retrieval of localStorage objects
         *
         * @param {String} key Index of storage object
         *
         * @returns {Object|String|Boolean}
         */
        get: function(key) {
          return window.localStorage.getItem(key);
        },

        /**
         * @function remove
         * @abstract Handle setting & retrieving of localStorage objects
         *
         * @param {String} key Index of storage object
         *
         */
        remove: function(key) {
          window.localStorage.removeItem(key);
        }

      },

      /**
       * @method session
       * @abstract Method for handling setting & retrieving of sessionStorage objects
       */
      session: {

        /**
         * @function set
         * @abstract Set session storage objects
         *
         * @param {String} key Index of storage object
         * @param {Object} data Data to be stoed
         *
         */
        set: function(key, data) {
          window.sessionStorage.setItem(key, data);
        },

        /**
         * @function get
         * @abstract Retrieves sessionStorage objects
         *
         * @param {String} key Index of storage object
         *
         * @returns {Object|String|Boolean}
         */
        get: function(key) {
          return window.sessionStorage.getItem(key);
        },

        /**
         * @function remove
         * @abstract Remove sessionStorage object
         *
         * @param {String} key Index of storage object
         *
         */
        remove: function(key) {
          window.sessionStorage.removeItem(key);
        }
      }
    };

    /**
     * @method crypto
     * @abstract Interface to handle encryption option
     */
    var crypto = crypto || {

      /**
       * @function key
       * @abstract Prepares key for encryption/decryption routines
       *
       * @param {String} pass Optional passphrase
       *
       * @returns {String}
       */
      key: function(pass) {
        pass = pass || this.muid();

        var salt = crypto.salt(pass);

        return sjcl.codec.hex.fromBits(sjcl.misc.pbkdf2(pass, salt));
      },

      /**
       * @function muid
       * @abstract Generates a machine identifier
       *
       * @returns {String}
       */
      muid: function() {
        var ret = window.navigator.appName +
          window.navigator.language +
          window.navigator.platform;

        return ret;
      },

      /**
       * @function salt
       * @abstract Creates salt from string & iv
       *
       * @param {String} str Machine identification used as salt
       *
       * @returns {String}
       */
      salt: function(str) {
        return sjcl.codec.base64.fromBits(
                sjcl.hash.sha256.hash(window.navigator.appName));
      }
    };

    /**
     * @method libs
     * @abstract Miscellaneous helper libraries
     */
    var libs = libs || {

      /**
       * @function total
       * @abstract Returns size of specified storage
       *
       * @param {String} engine Storage mechanism
       *
       * @returns {Insteger}
       */
      total: function(storage) {
        var current = '',
          engine = window.storage + 'Storage';

        for (var key in engine) {
          if (engine.hasOwnProperty(key)) {
            current += engine[key];
          }
        }

        return current ? 3 + ((current.length * 16) / (8 * 1024)) : 0;
      },

      /**
       * @function size
       * @abstract Perform calculation on objects
       *
       * @param {Object|Array} obj The object/array to calculate
       *
       * @returns {Integer}
       */
      size: function(obj) {
        var n = 0;

        if (/object/.test(typeof(obj))) {
          for (var i in obj) {
            if (obj.hasOwnProperty(obj[i])) n++;
          }
        } else if (/array/.test(typeof(obj))) {
          n = obj.length;
        }
        return n;
      },

      /**
       * @function merge
       * @abstract Perform preliminary option/default object merge
       *
       * @param {Object} defaults Application defaults
       * @param {Object} obj User supplied object
       *
       * @returns {Object}
       */
      merge: function(defaults, obj) {
        defaults = defaults || {};

        for (var item in defaults) {
          if (defaults.hasOwnProperty(item)) {
            obj[item] = (/object/.test(typeof(defaults[item]))) ?
              this.merge(obj[item], defaults[item]) : defaults[item];
          }
          obj[item] = defaults[item];
        }

        return obj;
      }
    };

    /**
     * @function get
     * @abstract Retrieves storage engine data
     *
     * @param {Object} obj User supplied options
     * @param {String} key Key of storage object to retrieve
     *
     * @returns {Object|null}
     */
    cryptio.prototype.get = function(obj, key) {
      var opts = libs.merge(obj, defaults);

      setup.init(opts);

      return storage.get(opts, key);
    };

    /**
     * @function set
     * @abstract Saves data to specified storage engine
     *
     * @param {Object} obj User supplied options
     * @param {String} key Key of storage object to retrieve
     * @param {Object} data User provided data to store
     *
     * @returns {Boolean}
     */
    cryptio.prototype.set = function(obj, key, data) {
      var opts = libs.merge(obj, defaults);

      setup.init(opts);

      return storage.set(opts, key, data);
    };

    /**
     * @function remove
     * @abstract Remove key from specified storage engine
     *
     * @param {Object} obj User supplied options
     * @param {String} key Key of storage object to retrieve
     *
     * @returns {Boolean}
     */
    cryptio.prototype.remove = function(obj, key) {
      var opts = libs.merge(obj, defaults);

      setup.init(opts);

      return storage.remove(opts, key);
    };

  };

  /* crypt.io, do work */
  window.cryptio = new cryptio;

})(window);
