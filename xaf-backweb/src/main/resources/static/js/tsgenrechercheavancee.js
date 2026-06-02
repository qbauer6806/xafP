$(document).ready(function () {

  configurationDataTable.order = [0]

  var default_category = "Autres"

  APP.selectedField = null

  APP.buildFacets = function () {
    var facetParams = self.buildFacetsSerachData({})

    var orderAndInsertFacets = function (categories, facetsByCat) {
      var categoriesArray = Object.values(categories)
      categoriesArray.sort(function (a, b) {
        var contentA = $(a).html();
        var contentB = $(b).html();
        return (contentA < contentB) ? -1 : (contentA > contentB) ? 1 : 0;
      });

      for (var i = 0; i < categoriesArray.length; i = i + 6) {
        var endSubTab = i + 6;
        if (i + 5 >= categoriesArray.length) {
          endSubTab = categoriesArray.length;
        }
        $("#rechercheavancee").append(
            $("<div class='row' style='margin-bottom:1em'>").append(
                categoriesArray.slice(i, endSubTab)))

      }

      for (cat in facetsByCat) {
        facetsByCat[cat].sort(function (a, b) {
          var contentA = $(a).find("#facetName")[0].innerText;
          var contentB = $(b).find("#facetName")[0].innerText;
          return (contentA < contentB) ? -1 : (contentA > contentB) ? 1 : 0;
        });

        $("#" + cat).append(facetsByCat[cat])
      }
    }

    $.ajax({
      url: APP.getContextPath() + "/ws/demandes/recherchechamps",
      method: "GET",
      traditional: true,
      data: facetParams
    }).done(function (facets) {

      $("#rechercheavancee").empty()

      var linkIdSuffix = ".link"

      var facetClick = function () {

        $("#rechercheavancee  a").removeClass("active-facet")
        $(this).addClass("active-facet")
        APP.selectedField = $(this).attr('id').replace(linkIdSuffix, '');
        $('#datatable-demandes').DataTable().ajax.reload(null, true);

      }

      var categories = []
      var facetsByCat = {}
      for (let f in facets) {
        var facet = facets[f];
        if (facet.enabled) {
          var facetName = facet.name;
          var categoryLibelle = default_category
          var categoryId = default_category
          var facetLibelle = facet.label || facetName;
          facetName = facetName.replace(/'/g, "")
          var facetLink = facetName + linkIdSuffix
          if (facet.categoryId) {
            var category = facet.allCategories.find(obj => {
              return obj.id === facet.categoryId
            });
            categoryLibelle = category ? category.label : default_category;
            categoryId = facet.categoryId;
          }

          if (!categories[categoryId]) {
            var newfacetCategoryDiv = $("#facetCategory").clone();
            newfacetCategoryDiv.attr("id", categoryId)
            newfacetCategoryDiv.find("#facetCategoryName").html(
                categoryLibelle);
            newfacetCategoryDiv.show()
            categories[categoryId] = newfacetCategoryDiv
            facetsByCat[categoryId] = []
          }

          var newfacetDiv = $("#facet").clone();
          newfacetDiv.attr("id", facetName).find("#facetName").html(
              facetLibelle.replace(/'/g, "&apos;"));
          newfacetDiv.find("#facetLink").attr("id", facetLink).on("click",
              facetClick);
          facetsByCat[categoryId].push(newfacetDiv);
          newfacetDiv.show();
        }
      }
      orderAndInsertFacets(categories, facetsByCat)

    }).fail(function (ex) {
      if (ex.status === 500 || ex.status === 404 || ex.status === 403) {
        window.location.href = APP.getContextPath() + '/error/' + ex.status;
      }
    });

  }
});

